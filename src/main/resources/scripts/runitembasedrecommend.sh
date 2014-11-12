#!/bin/bash
echo "Running 5 Item Based Recommendations for Movies" 
#hadoop jar mahout-core-0.9.0.2.1.1.0-385-job.jar org.apache.mahout.cf.taste.hadoop.similarity.item.ItemSimilarityJob --input -i /apps/movielens/input/ratings.csv --ooutput /apps/movielens/output --similarityClassname SIMILARITY_LOGLIKELIHOOD --maxSimilaritiesPerItem 5
#itemsimilarity
hadoop fs -rmr /user/**/temp/*
mahout itemsimilarity -s SIMILARITY_LOGLIKELIHOOD -i /apps/movielens/input/ratings.csv -o /apps/movielens/itemitemoutput --maxSimilaritiesPerItem 5

