#!/bin/bash
# run client
CP="dist/pa2.jar:.:lib/log4j-1.2.16.jar:lib/commons-lang3-3.1.jar:lib/commons-lang-2.3.jar:lib/commons-configuration-1.8.jar:lib/commons-logging-1.1.1.jar"
FILESERVER_MAIN="pa2.FileServer"
DEFS="-Djava.rmi.server.codebase=file://public/pa2.jar -Djava.rmi.server.hostname=localhost"

COLLECTOR_IP=127.0.0.1
COLLECTOR_PORT=44555

NUM_PROCS=$1
echo "NUM PROCS: "${NUM_PROCS}
# start file servers
#for i in  `seq 1 $1`
for i in `jot $1 1`
do
    echo "starting a file server with collector at " $COLLECTOR_IP ":" $COLLECTOR_PORT
    java -cp $CP $DEFS $FILESERVER_MAIN $COLLECTOR_IP $COLLECTOR_PORT &
done


