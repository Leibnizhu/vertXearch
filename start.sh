#!/bin/bash
nohup java -jar vertXearch-0.0.1-fat.jar ./config.json  2>&1 1>vertXearch.log &
ps -ef|grep vertXearch|grep -v grep|awk '{print $2}' > pid