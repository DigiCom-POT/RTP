Apache Spark and Spark Streaming example
========================================


Note : I have used hortonworks sandbox and installed Apache Spark and Spark Streaming.



Spark Streaming + Flume (read the file from flume agent using avro sink)
--------------------------------------------------------------------

***To Start the Avro Sink***
```sh
flume-ng agent -c /etc/flume/conf -f /etc/flume/conf/flumeavro.conf -n sandbox
```

***To run  Spark Streaming example***
```sh
./bin/spark-submit examples/realtimepersonalization-spark-0.0.1-SNAPSHOT-jar-with-dependencies.jar --class digicom.pot.rtp.spark.streaming.TopRatedMovieAggregation 127.0.0.1 41414
```


NOTE:
----
* flumeavro.conf is checked in resource folder
* createRuntime.sh to read omniturelog and write to another log file to simulate real time streaming
* Do mvn package to build the jar with dependencies

Sources:
-------
* Hortworks Tutorial
* Spark Streaming examples
