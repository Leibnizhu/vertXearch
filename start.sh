#!/bin/bash
nohup java -jar vertxearch-0.0.1-fat.jar ./config.json  2>&1 1>vertxearch.log &
ps -ef|grep vertxearch|grep -v grep|awk '{print $2}' > pid