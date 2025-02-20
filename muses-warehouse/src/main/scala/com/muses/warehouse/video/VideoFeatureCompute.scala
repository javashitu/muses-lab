package com.muses.warehouse.video

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{MinMaxScaler, OneHotEncoder, StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf

object VideoFeatureCompute {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("video feature compute").master("local[*]").getOrCreate()
    import sparkSession.implicits._

    val videoTagDataFrame = sparkSession.read
      .format("jdbc")
      .option("driver", "com.github.housepower.jdbc.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:9109:9109/muses_warehouse")
      .option("user", "default")
      .option("dbtable", "video_tag")
      .load

    val vectorAssembler = new VectorAssembler().setInputCols(Array("resolution", "frame_rate", "bit_rate", "duration")).setOutputCol("features")
    val minMaxScaler = new MinMaxScaler().setInputCol("features").setOutputCol("scaleFeatures")
    val stringIndex = new StringIndexer().setInputCol("topic_tag").setOutputCol("topic_tag_index")
    val oneHotEncoder = new OneHotEncoder().setInputCol("topic_tag_index").setOutputCol("video_tag_vec")
    val allFeatureVectorAssembler = new VectorAssembler().setInputCols(Array("scaleFeatures", "video_tag_vec")).setOutputCol("video_feature_vec")

    val numOneHotEncoder = new OneHotEncoder().setInputCols(Array("resolution", "frame_rate", "bit_rate", "duration")).setOutputCols(Array("resolution_vec", "frame_rate_vec", "bit_rate_vec", "duration_vec"))
    val featureVectorAssembler = new VectorAssembler().setInputCols(Array("resolution_vec", "frame_rate_vec", "bit_rate_vec", "duration_vec", "video_tag_vec")).setOutputCol("video_feature_vec")

    //    val pipeline = new Pipeline().setStages(Array(vectorAssembler, minMaxScaler, stringIndex, oneHotEncoder, allFeatureVectorAssembler))
    val pipeline = new Pipeline().setStages(Array(numOneHotEncoder, stringIndex, oneHotEncoder, featureVectorAssembler))

    val videoTagVecFeature = pipeline.fit(videoTagDataFrame).transform(videoTagDataFrame)
    videoTagVecFeature.show()

    val vector2Str: Any => String = vector => vector.asInstanceOf[org.apache.spark.ml.linalg.Vector].toArray.mkString(";")
    val vector2StrFun = udf(vector2Str)

    val videTagVector = videoTagVecFeature.withColumn("video_feature_vec", vector2StrFun($"video_feature_vec"))
    videTagVector.select("video_id","video_feature_vec").show()

    videTagVector.select("video_id", "video_feature_vec").toDF("video_id", "features").write
      .format("jdbc")
      .option("driver", "ru.yandex.clickhouse.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:8123/muses_lab")
      .option("dbtable", "muses_lab.video_feature_vector")
      .option("user", "default")
      .mode("Append")
      .save()
  }
}
