package com.muses.warehouse.video

import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.udf
import org.jpmml.model.JAXBUtil
import org.jpmml.sparkml.PMMLBuilder

import java.io.FileOutputStream
import javax.xml.transform.stream.StreamResult

object VideoLRClassify {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("Video Logistic Regression Classify Compute").master("local[*]").getOrCreate()
    sparkSession.sparkContext.setLogLevel("warn")
    import sparkSession.implicits._

    val sql =
      """
        |(
        |with t1 as(
        |select vul.*,vfv.features as video_vector from video_user_label vul left join video_feature_vector vfv on vul.video_id = vfv.video_id
        |),
        |t2 as(
        |select t1.*,ufv.features as user_vector from t1 left join user_feature_vector ufv on t1.user_id = ufv.user_id
        |)
        |select * from t2
        |) as tmp
        |""".stripMargin

    val sourceDataFrame = sparkSession.read
      .format("jdbc")
      .option("driver", "com.github.housepower.jdbc.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://172.18.57.230:9109/muses_lab")
      .option("user", "default")
      .option("dbtable", sql)
      .load
    sourceDataFrame.show()

    val colMerge = udf((col1: String, col2: String) => {
      col1 + ";" + col2
    })
    val str2arr = udf((str: String) => str.split(";").filter(_.nonEmpty).map(num => num.toDouble))

    val mergeDataFrame = sourceDataFrame.withColumn("merge_vector", colMerge($"video_vector", $"user_vector"))
      .withColumn("features", str2arr($"merge_vector"))
    val splitDataFrame = mergeDataFrame.select($"label" +: (1 to 15).map(index => $"features".getItem(index - 1).as(s"f$index")): _*)

    val vectorAssembler = new VectorAssembler().setInputCols(Array((1 to 10).map(index => s"f$index"): _*)).setOutputCol("feature_vector")
    val lr = new LogisticRegression().setFeaturesCol("feature_vector").setLabelCol("label").setMaxIter(10).setRegParam(0.1)

    val pipelineModel = new Pipeline().setStages(Array(vectorAssembler, lr)).fit(splitDataFrame)

    val pmml = new PMMLBuilder(splitDataFrame.schema, pipelineModel).build()
    JAXBUtil.marshalPMML(pmml, new StreamResult(System.out))
    JAXBUtil.marshalPMML(pmml, new StreamResult(new FileOutputStream("lr.pmml")))

  }
}
