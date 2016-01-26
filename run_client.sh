#!/bin/bash
# run client
CP="dist/pa2.jar:.:lib/log4j-1.2.16.jar:lib/commons-lang3-3.1.jar:lib/commons-lang-2.3.jar:lib/commons-configuration-1.8.jar:lib/commons-logging-1.1.1.jar"
CLIENT_MAIN="pa2.Client"
DEFS="-Djava.rmi.server.codebase=file://public/pa2.jar -Djava.rmi.server.hostname=localhost"

COLLECTOR_IP=127.0.0.1
COLLECTOR_PORT=44555

# start client
if [[ -n $2 ]]
then 
    java -cp $CP $DEFS $CLIENT_MAIN  $COLLECTOR_IP $COLLECTOR_PORT $1 "$2"
elif [[ -n $1 ]]
then
    java -cp $CP $DEFS $CLIENT_MAIN  $COLLECTOR_IP $COLLECTOR_PORT $1
else
    java -cp $CP $DEFS $CLIENT_MAIN  $COLLECTOR_IP $COLLECTOR_PORT
fi

