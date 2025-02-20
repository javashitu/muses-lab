package com.muses.warehouse.video

import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.catalyst.util.IntervalUtils.IntervalUnit.DAY
import org.apache.spark.sql.functions.{lit, udf}
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.concurrent.TimeUnit

object UserIdEmbeddingComppute {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("Video embedding Compute").master("local[*]").getOrCreate()
    sparkSession.sparkContext.setLogLevel("warn")
    import sparkSession.implicits._

    val sourceDataFrame = sparkSession.read
      .format("jdbc")
      .option("driver", "com.github.housepower.jdbc.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:9109/muses_lab")
      .option("user", "default")
      .option("dbtable", "video_event")
      .load

    val videoScoreDataFrame = computeItemScore(sparkSession, sourceDataFrame)
    videoScoreDataFrame.show()
    val als = new ALS().setMaxIter(5).setRegParam(0.01).setItemCol("video_id").setUserCol("user_id").setRatingCol("score")
      .setColdStartStrategy("drop").setNonnegative(true).setImplicitPrefs(true).setRank(32)
    val aslModel = als.fit(videoScoreDataFrame)

    //    println("the video factors is ")
    //    println(aslModel.itemFactors)
    //    aslModel.itemFactors.show()
    val joinStr = udf((arr: Any) => arr.asInstanceOf[Seq[Float]].mkString(";"))
    //    val arrLength = udf((arr: Seq[Float])=> arr.length)
    //    println("show the join features")
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDate = currentDate.format(formatter)
    //TODO 应该把昨天的embedding取出来求加权平均，
    // 可以吧昨天的embedding查出来两个dataframe拼接在一起，再新加一个权重字段，有值就为1，无值就位0，
    // 最后sum(embedding)除以这个权重
    val yesterday = currentDate.plus(-1L, ChronoUnit.DAYS).format(formatter)


    val videoEmbedding = aslModel.userFactors.withColumn("embedding", joinStr($"features"))
      .withColumn("create_date", lit(formattedDate))
      .withColumn("video_id", $"id")
      .select("video_id", "embedding", "create_date")
    videoEmbedding.show()

    videoEmbedding.write
      .format("jdbc")
      .option("driver", "ru.yandex.clickhouse.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:8123/muses_lab")
      .option("dbtable", "muses_lab.video_embedding")
      .option("user", "default")
      .mode("Append")
      .save()
  }

  private def computeItemScore(sparkSession: SparkSession, sourceDataFrame: DataFrame): DataFrame = {
    sourceDataFrame.createOrReplaceTempView("source_data")
    val computeRating = (action: String) => {
      action match {
        case "click" => 0.2
        case "like" => 0.2
        case "share" => 0.2
        case "coin" => 0.2
        case "collect" => 0.2
      }
    }
    sparkSession.udf.register("computeRating", computeRating)
    val sql =
      """
        | select cast(user_id as bigint),cast(video_id as bigint),sum(computeRating(event)) as score from source_data group by user_id, video_id
        |""".stripMargin
    sparkSession.sql(sql)
  }
}
