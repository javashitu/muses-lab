package com.muses.warehouse.video

import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.functions.{explode, udf}
import org.apache.spark.sql.{DataFrame, SparkSession}

object VideoALSCompute {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("Video ALS Compute").master("local[*]").getOrCreate()
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
//    val joinStr = udf((arr : Any) => arr.asInstanceOf[Seq[Float]].mkString(";"))
//    val arrLength = udf((arr: Seq[Float])=> arr.length)
//    println("show the join features")
//    aslModel.userFactors.withColumn("join_features", joinStr($"features")).withColumn("features_length", arrLength($"features")).show()


    val recommendVideo = aslModel.recommendForAllUsers(20)
    recommendVideo.show()

    println("next will replace column")
    val explodeRecommendVideo = recommendVideo.withColumn("recommend_video", explode($"recommendations"))
      .withColumn("video_id",$"recommend_video.video_id")
      .withColumn("rating",$"recommend_video.rating")

//    explodeRecommendVideo.show()
    explodeRecommendVideo.createOrReplaceTempView("recommend_video")
    videoScoreDataFrame.createOrReplaceTempView("video_score")
    val topK = 10
    val chooseVideoSql =
      s"""
        |with t3 as(
        |select t1.user_id, t1.video_id, t1.rating
        |from recommend_video as t1 left join video_score as t2 on t1.user_id = t2.user_id and t1.video_id = t2.video_id
        |where t2.score is null
        |),
        |t4 as(
        |select t3.*, row_number() over (partition by user_id order by rating) as row_no from t3
        |)
        |select user_id,video_id,rating from t4 where row_no <= $topK
        |""".stripMargin

    sparkSession.sql(chooseVideoSql).show()

    sparkSession.sql(chooseVideoSql).write
      .format("jdbc")
      .option("driver", "ru.yandex.clickhouse.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:8123/muses_lab")
      .option("dbtable", "muses_lab.video_als")
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
