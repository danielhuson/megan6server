#!/bin/bash
STOPPORT="8181"
STOPKEY="7ce31e4eb617c85efbff9450bf9c2fd2"
HELP="N"
while [[ $# > 0 ]]
do
key="$1"
case $key in
    --stop-port)
    STOPPORT="$2"
    shift # past argument
    ;;
    --stop-key)
    STOPKEY="$2"
    shift # past argument
    ;;
    --help)
    echo -e "Usage\n--stop-port\tPort to stop MeganServer. Default=8181\n--stop-key\tStop key for stopping MeganServer. Default=7ce31e4eb617c85efbff9450bf9c2fd2\n"
    exit
    shift # past argument
    ;;
    *)
    ;;
  esac
  shift # past argument or value
  done

echo Stopping MeganServer with following options:


echo Stop Port = ${STOPPORT}
echo Stop Key = ${STOPKEY}


java -jar ../lib/start.jar STOP.PORT=${STOPPORT} STOP.KEY=${STOPKEY} --stop

echo Shut down MeganServer
