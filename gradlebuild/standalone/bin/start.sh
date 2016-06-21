#!/bin/bash
PORT="8080"
STOPPORT="8181"
STOPKEY="7ce31e4eb617c85efbff9450bf9c2fd2"
XMX="4G"
NAME="MeganServer"


while [[ $# > 0 ]]
do
key="$1"
case $key in
    --help)
    echo -e "Usage\n--name\t\tDefine name of MeganServer instance. Default=Meganserver\n--port\t\tPort on which MeganServer will be launched. Default=8080\n--stop-port\tPort to stop MeganServer. Default=8181\n--stop-key\tStop key for stopping MeganServer. Default=7ce31e4eb617c85efbff9450bf9c2fd2\n--max-memory\tMax memory for MeganServer. Default=4G\n"
    shift # past argument
    exit
    ;;
    --name)
    NAME="$2"
    shift # past argument
    ;;
    --port)
    PORT="$2"
    shift # past argument
    ;;
    --stop-key)
    STOPPORT="$2"
    shift # past argument
    ;;
    --stop-key)
    STOPKEY="$2"
    shift # past argument
    ;;
    --max-memory)
    XMX="$2"
    shift # past argument
    ;;
    *)
    ;;
  esac
  shift # past argument or value
  done

echo Launching MEGANServer with following options:

echo Port = ${PORT}
echo Stop Port = ${STOPPORT}
echo Stop Key = ${STOPKEY}
echo Max Memory = ${XMX}
echo Name = ${NAME}



inUse=$(netstat -an | grep ${PORT} | grep -i listen | wc -l)
if [ $inUse = 1 ]; then
    echo "The port ${PORT} is already in use. Server cannot start. A different port can be set by using the -p option"
    exit
fi

inUse=$(netstat -an | grep ${STOPPORT} | grep -i listen | wc -l)
if [ $inUse = 1 ]; then
    echo "The stop port ${STOPPORT} is already in use. Server cannot start. A different stop port can be set by using the -sp option"
    exit
fi



echo "Starting MeganServer..."
java -Xmx${XMX} -jar ../lib/jetty-runner-9.2.7.v20150116.jar --out ../log/yyyy_mm_dd.log --classes ../properties/ --port ${PORT} --path /$NAME ../lib/MeganServer.war --stop-port ${STOPPORT} --stop-key ${STOPKEY}&

echo "MeganServer started. Reachable under http://www.localhost:${PORT}/${NAME}"
