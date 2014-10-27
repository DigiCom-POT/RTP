#!/bin/bash
while read line;
do echo "$line" >> /var/log/eventlog-demo.log;
sleep 0.5;
done < 0.tsv
