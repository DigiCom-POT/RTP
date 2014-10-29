Real and Batch Time Personalization 
===================================

**Set up**

**DataSet Used for the use case**
>Downloaded the 10 m user / ratings (http://files.grouplens.org/datasets/movielens/ml-10m.zip)
>It is also available in the movielensdata folder under resource directory

Download the [hortonworks sandbox](http://hortonworks.com/products/hortonworks-sandbox/) image and Virtual Box to setup the Hadoop ecosystem 

Sandbox comes with lots of inbuilt packages like Hbase, Storm, Zookeeper, Thrift, Hive, Hue etc. 

Some of external packages you need to install for running the code. 

* Install Apache Spark and Spark Streaming.
* Install Cassandra Community Edition.
* Install Apache Flume.
* Install Mahout.
* (You can also migrate some of the code using Storm / Kafka / Anyother NoSQL )

[You need atleast 8 GB Ram with 20 GB of free space to run the hortonwork sandbox efficiently]

Batch Personalization
---------------------
Extract the movie lens data in a folder and scp it to sandbox.

The files are pipe (|) separated and with an additional field of timestamp.

You can clean up the data using following command
```
#>tr -s ':' ',' < ratings.dat | cut -f1-3 -d, > ratings.csv
```

Now copy this file to HDFS from the local machine.
```
#>Hadoop fs –put ratings.csv /app/movielens/input
```

**User Based Recommendation**

Run the script under resources/script folder - runuserbasedrecommend.sh. 
This will create the output recommendation file in the HDFS file system.  
```
mahout recommenditembased -s SIMILARITY_LOGLIKELIHOOD -i /apps/movielens/input/ratings.csv -o /apps/movielens/output --numRecommendations 6
```

**Item Based Recommendation**

Run the script under resources/script folder - runitembasedrecommend.sh. 
This will create the output recommendation file in the HDFS file system.  
```
hadoop jar mahout-core-0.9.0.2.1.1.0-385-job.jar org.apache.mahout.cf.taste.hadoop.similarity.item.ItemSimilarityJob --input -i /apps/movielens/input/ratings.csv --ooutput /apps/movielens/output --similarityClassname SIMILARITY_LOGLIKELIHOOD --maxSimilaritiesPerItem 5
```

For our use case we have used simple algorithms like SIMILARITY_LOGLOLELIHOOD to derive the recommendation. Mahout comes with lots of algorithms which can be applied based on the specific requirements. You can also write your own recommendation algorithm for specific use cases.

Once the recommendations are generated. We need to put the output to some NoSQL or structured database so that it can retrieve quickly and applied in the application. For the sample use case we are loading the data in HBase and accessing it using stargate Hbase rest client.

**For loading the data in Hbase**

We have created three tables using HCAT and HBaseHCatStorageHandler for
- MoviesInfo.ddl - ( data from the movie lens folder having movie id , movie name and its genere information )
- UserBasedRecommendation.ddl - ( the output from the userbased recommendation algorithm)
- ItemBasedRecommenation.ddl - ( the output from the itembased recommendation algorithm)

The hcat scripts are available under resources/hact folder. Execute the script in sandbox.
```
hcat –f MoviesInfo.ddl
hcat –f UserBasedRecommendation.ddl
hcat –f ItemBasedRecommenation.ddl
```

Once the table is created you can bulk load the data to Hbase table using PIG scripts.
Pig scripts are available in resources\pig folder.
```
pig –x local loadMoviesInfo.txt
pig –x local loadUserRecommendation.txt
pig –x local loadSimiliarMovies.txt

```

We can analyze the result either by logging to hbase shell or start the hbase rest service and access it using the rest client.

```
starting service : ./bin/hbase-daemon.sh start rest -p 8500
stopping service : ./bin/hbase-daemon.sh stop rest -p 8500

```

Now from any rest client we can make call to get the information.
Some examples :

```
http://localhost:8500/moviesinfo/<movieid>
http://localhost:8500/moviesrecommend/<userid>
http://localhost:8500/movmovrec/<movieid>
```

[Note : The data in json response may be base 64 encoded.]

Real Time Personalization
-------------------------



**Spark Streaming + Flume (read the file from flume agent using avro sink)**





***To Start the Avro Sink***
```
flume-ng agent -c /etc/flume/conf -f /etc/flume/conf/flumeavro.conf -n sandbox
```

***To run  Spark Streaming example***
```
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
