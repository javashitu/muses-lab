package com.muses.warehouse.video

import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.sql.{DataFrame, SparkSession}

object VideoItemCFCompute {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("item cf compute").master("local[*]").getOrCreate()
    sparkSession.sparkContext.setLogLevel("warn")
    val dataFrame = sparkSession.read
      .format("jdbc")
      .option("driver", "com.github.housepower.jdbc.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:9109/muses_lab")
      .option("user", "default")
      .option("dbtable", "video_event")
      .load
    import sparkSession.implicits._

    val sourceScoreDataFrame = computeItemScore(sparkSession, dataFrame)
    sourceScoreDataFrame.show()
    sourceScoreDataFrame.createOrReplaceTempView("video_score")

    val matrixEntry = sourceScoreDataFrame.rdd.map(row => {
      MatrixEntry(row.getAs[Long](0), row.getAs[Long](1), row.getAs[Double](2))
    })
    val coordinateMatrix = new CoordinateMatrix(matrixEntry)
    val similarMatrix = coordinateMatrix.toRowMatrix().columnSimilarities()
    val similarDataFrame = similarMatrix.entries.map(entry => {
      (entry.i, entry.j, entry.value)
    }).toDF("video_id", "similar_video_id", "similar_rating")

    val videoSimilarDataFrame = similarDataFrame.union(similarDataFrame.select("similar_video_id", "video_id", "similar_rating"))
    videoSimilarDataFrame.createOrReplaceTempView("video_similar")
    //+--------+----------------+------------------+
    //|video_id|similar_video_id|    similar_rating|
    //+--------+----------------+------------------+
    //|  764801|          764802|0.4472135954999579|
    //|  764800|          764802|               1.0|
    //|  764800|          764801|0.4472135954999579|
    //|  764802|          764801|0.4472135954999579|
    //|  764802|          764800|               1.0|
    //|  764801|          764800|0.4472135954999579|
    //+--------+----------------+------------------+
    videoSimilarDataFrame.show()

    val joinSql =
      """
        |select t1.user_id, t1.video_id, t1.score, t2.video_id, t2.similar_video_id, t2.similar_rating, (t1.score * t2.similar_rating) as similar_score
        |from video_score t1 left join video_similar t2
        |on t1.video_id = t2.video_id
        |where t2.similar_rating is not null
        |""".stripMargin
    val similarRatingDataFrame = sparkSession.sql(joinSql)
    similarRatingDataFrame.sort("t1.user_id", "t1.video_id")
    //+-------+--------+-----+--------+----------------+------------------+-------------------+
    //|user_id|video_id|score|video_id|similar_video_id|    similar_rating|        final_score|
    //+-------+--------+-----+--------+----------------+------------------+-------------------+
    //|   9527|  764801|  0.2|  764801|          764802|0.4472135954999579|0.08944271909999159|
    //|   9527|  764801|  0.2|  764801|          764800|0.4472135954999579|0.08944271909999159|
    //|   9528|  764801|  0.4|  764801|          764802|0.4472135954999579|0.17888543819998318|
    //|   9528|  764801|  0.4|  764801|          764800|0.4472135954999579|0.17888543819998318|
    //|   9527|  764802|  0.2|  764802|          764801|0.4472135954999579|0.08944271909999159|
    //|   9527|  764802|  0.2|  764802|          764800|               1.0|                0.2|
    //|   9527|  764800|  1.0|  764800|          764802|               1.0|                1.0|
    //|   9527|  764800|  1.0|  764800|          764801|0.4472135954999579| 0.4472135954999579|
    //+-------+--------+-----+--------+----------------+------------------+-------------------+
    similarRatingDataFrame.show()

   val recommendDataFrame = chooseSimilarScore(sparkSession, similarRatingDataFrame, sourceScoreDataFrame)
    //+-------+----------------+-------------------+
    //|user_id|similar_video_id|        final_score|
    //+-------+----------------+-------------------+
    //|   9528|          764802|0.17888543819998318|
    //|   9528|          764800|0.17888543819998318|
    //+-------+----------------+-------------------+
    recommendDataFrame.show()

    recommendDataFrame.write
      .format("jdbc")
      .option("driver", "ru.yandex.clickhouse.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:8123/muses_lab")
      .option("dbtable", "muses_lab.video_itermcf")
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

  private def chooseSimilarScore(sparkSession: SparkSession, similarScoreDataFrame: DataFrame, sourceScoreDataFrame: DataFrame): DataFrame = {
    similarScoreDataFrame.createOrReplaceTempView("similar_score")
    sourceScoreDataFrame.createOrReplaceTempView("source_score")
    val recommendSql =
      """
        | with t1 as (
        | select user_id, similar_video_id, sum(similar_score) as final_score from similar_score group by user_id, similar_video_id
        | ),
        | t2 as(
        |   select t1.* from t1 left join source_score on t1.similar_video_id = source_score.video_id and t1.user_id = source_score.user_id where source_score.score is null
        | ),
        | t3 as(
        |   select user_id, similar_video_id, final_score,
        |   row_number() over(partition by user_id order by final_score desc) as row_no
        |   from t2
        | )
        | select user_id, similar_video_id as video_id, final_score as score from t3 where row_no < 10
        |""".stripMargin
    sparkSession.sql(recommendSql)

  }
}
