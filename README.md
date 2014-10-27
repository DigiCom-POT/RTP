Apache Spark and Spark Streaming example
========================================


Note : I have used hortonworks sandbox and installed Apache Spark and Spark Streaming.


Running the simple app on Spark
-------------------------------

cd /home/spark/spark-1.0.1 (Spark Installation Directory)

```sh
./bin/spark-submit --class sagar.spark.example.SimpleApp examples/sagar-spark-0.0.1-SNAPSHOT.jar
```

Spark Streaming + Flume (read the file from flume agent using avro sink)
--------------------------------------------------------------------

***To Start the Avro Sink***
```sh
flume-ng agent -c /etc/flume/conf -f /etc/flume/conf/flumeavro.conf -n sandbox
```

***To run  Spark Streaming example***
```sh
./bin/spark-submit examples/sagar-spark-0.0.1-SNAPSHOT-jar-with-dependencies.jar --class sagar.spark.streaming.example.JavaFlumeEventCount 127.0.0.1 41414
```

Spark Streaming + Kafka
----------------------
* Install Kafka on hortonworks sandbox
* Run JavaKafkaWordCount to listen to truckevent topic

```sh
./bin/spark-submit examples/sagar-spark-0.0.1-SNAPSHOT-jar-with-dependencies.jar class sagar.spark.streaming.example.JavaKafkaWordCount localhost:2181 mygroup truckevent 1
```
 
* Use Kafka tools to push the message to topic (truckevent)

```sh
 bin/kafka-console-producer.sh --broker-list localhost:9092 --topic truckevent
```


NOTE:
----
* flumeavro.conf is checked in resource folder
* createRuntime.sh to read omniturelog and write to another log file to simulate real time streaming
* Do mvn package to build the jar with dependencies
* Start kafka Service ( to run the kafka exmaple : service kafka start/stop

Sources:
-------
* Hortworks Tutorial
* Spark Streaming examples
