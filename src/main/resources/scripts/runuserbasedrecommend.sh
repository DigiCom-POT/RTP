#!/bin/bash
echo "Running 6 User Based Recommendations for Movies" 
hadoop fs -rmr /user/**/temp/*
mahout recommenditembased -s SIMILARITY_LOGLIKELIHOOD -i /apps/movielens/input/ratings.csv -o /apps/movielens/output --numRecommendations 6

