#!/bin/bash
# run client
CP="dist/pa2.jar:.:lib/log4j-1.2.16.jar:lib/commons-lang3-3.1.jar:lib/commons-lang-2.3.jar:lib/commons-configuration-1.8.jar:lib/commons-logging-1.1.1.jar"
COLLECTOR_MAIN="pa2.Collector"
DEFS="-Djava.rmi.server.codebase=file://public/pa2.jar -Djava.rmi.server.hostname=localhost"

COLLECTOR_IP=127.0.0.1
COLLECTOR_PORT=44555

# start collector
echo "starting collector at "$COLLECTOR_IP":"$COLLECTOR_PORT
java -cp $CP $DEFS $COLLECTOR_MAIN $COLLECTOR_IP $COLLECTOR_PORT &
