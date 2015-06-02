#!/bin/bash
inUse=$(netstat -an | grep 8080 | grep -i listen | wc -l)
if [ $inUse = 1 ]; then
    echo "The port 8080 is already in use. Server cannot start."
    exit
fi

name=$1
if [ $# -eq 0 ]
then
    echo "start.sh was executed without parameters. Will start MEGANServer at standard location."
    name="MeganServer"
fi



echo "Starting webserver -> reachable in your browser under localhost:8080/$name"
echo
java -jar ../lib/jetty-runner-9.2.7.v20150116.jar --out ../log/yyyy_mm_dd.log --classes ../properties/ --path /$name ../lib/MeganServer.war --stop-port 8181 --stop-key 7ce31e4eb617c85efbff9450bf9c2fd2&


