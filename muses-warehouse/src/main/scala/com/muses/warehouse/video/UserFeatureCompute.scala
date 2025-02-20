package com.muses.warehouse.video

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{Bucketizer, MinMaxScaler, OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf

object UserFeatureCompute {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder()
      .appName("user feature compute")
      .master("local[*]")
      .getOrCreate()

    import sparkSession.implicits._

    val userInfoDataFrame = sparkSession.read
      .format("jdbc")
      .option("driver", "com.github.housepower.jdbc.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:9109/muses_warehouse")
      .option("user", "default")
      .option("dbtable", "user_info")
      .load

    val bucketizer = new Bucketizer().setInputCol("age").setOutputCol("range_age").setSplits(Array(0, 8, 16, 24, 35, 45, 60, 70, 80, Double.PositiveInfinity))
    val stringIndexer = new StringIndexer().setInputCols(Array("sex", "city")).setOutputCols(Array("sex_index", "city_index"))
    val oneHoeEncoder = new OneHotEncoder().setInputCols(Array("range_age","sex_index", "city_index")).setOutputCols(Array("age_vec","sex_vec", "city_vec"))
    val vectorAssembler = new VectorAssembler().setInputCols(Array("age_vec", "sex_vec", "city_vec")).setOutputCol("user_feature_vec")

    val pipeline = new Pipeline().setStages(Array(bucketizer, stringIndexer, oneHoeEncoder, vectorAssembler))
    val userVec = pipeline.fit(userInfoDataFrame).transform(userInfoDataFrame)

    val vector2Str: Any => String = vector => vector.asInstanceOf[org.apache.spark.ml.linalg.Vector].toArray.mkString(";")
    val vector2StrFun = udf(vector2Str)

    userVec.withColumn("features",vector2StrFun($"user_feature_vec")).select("user_id", "features").write
      .format("jdbc")
      .option("driver", "ru.yandex.clickhouse.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:8123/muses_lab")
      .option("dbtable", "muses_lab.user_feature_vector")
      .option("user", "default")
      .mode("Append")
      .save()
  }
}
