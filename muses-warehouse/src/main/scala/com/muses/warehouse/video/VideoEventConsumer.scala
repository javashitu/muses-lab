package com.muses.warehouse.video

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{IntegerType, LongType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, ConsumerStrategies, HasOffsetRanges, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.text.SimpleDateFormat
import java.util.Date

object VideoEventConsumer {
  def main(args: Array[String]): Unit = {
    val topic = "video_event"
    val group = "muses_warehouse"
    val brokers = "182.168.131.24:9092"

    val conf = new SparkConf().setAppName("video event consumer")
    conf.setMaster("local[*]")
    conf.set("spark.testing.memory", "2147480000")
    conf.set("spark.executor.instances", "4")
    conf.set("spark.executor.memory", "2g")
    conf.set("spark.driver.allowMultipleContexts", "true")
    //    conf.set("hive.metastore.uris", "thrift://192.168.120.24:9035")
//    conf.set("spark.sql.warehouse.dir", "hdfs://192.168.136.24:9000/opt/data/soft/hive/warehouse")

    val sparkSession = SparkSession.builder().config(conf).getOrCreate()
    //    val sqp = "show databases"
    val streamingContext = new StreamingContext(sparkSession.sparkContext, Seconds(5))

    streamingContext.sparkContext.setLogLevel("info")

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> brokers,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> group, //消费者组名
      "auto.offset.reset" -> "earliest", //latest自动重置偏移量为最新的偏移量
      "enable.auto.commit" -> "false"
    )

    val kafkaTopicDS = KafkaUtils.createDirectStream(
      streamingContext,
      LocationStrategies.PreferConsistent, //如果executor和broker在相同的机器上，则使用当前机器的broker，
      ConsumerStrategies.Subscribe[String, String](Set(topic), kafkaParams)
    )

    kafkaTopicDS.foreachRDD(rdd => {
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      for (ele <- offsetRanges) {
        println("partition is " + ele.partition + " the consume offset is " + ele.untilOffset)
      }
      val rowRdd = formatKafkaMsg(rdd)
      val dataFrame = sparkSession.createDataFrame(rowRdd, createSchema())
      dataFrame.show()
      saveDataFrame2CK(dataFrame)
      kafkaTopicDS.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
    })

    streamingContext.start()
    streamingContext.awaitTermination()
  }

  private def createSchema(): StructType = {
    StructType(
      List(
        StructField("video_id", StringType, false),
        StructField("user_id", StringType, false),
        StructField("event", StringType, false),
        StructField("operate_time", LongType, false),
        StructField("operate_date", StringType, false),
      )
    )
  }

  private val mapper = new ObjectMapper()
  private val formatter = new SimpleDateFormat("yyyy-MM-dd")

  private def formatKafkaMsg(rdd: RDD[ConsumerRecord[String, String]]): RDD[Row] = {
    rdd.filter(record=> StringUtils.isNotBlank(record.value())).map(record => {
      println("consume kafka value " + record.value())
      val jsonNode = mapper.readTree(record.value())
      val videoId = jsonNode.get("videoId").asText()
      val userId = jsonNode.get("userId").asText()
      val operateTime = jsonNode.get("timestamp").asLong()
      val operateEvent = jsonNode.get("event").asText()
      val createDate = formatter.format(new Date(operateTime))
      Row(videoId, userId, operateEvent, operateTime, createDate)
    })
  }

  private def saveDataFrame2CK(dataFrame: DataFrame): Unit = {
    dataFrame.write
      .format("jdbc")
      .option("driver", "ru.yandex.clickhouse.ClickHouseDriver")
      .option("url", "jdbc:clickhouse://192.168.136.24:8123/muses_warehouse")
      .option("dbtable", "video_event")
      .option("user", "default")
      .mode("Append")
      .save()
  }
}
