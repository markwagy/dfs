#!/bin/bash
# run client
CP="dist/pa2.jar:.:lib/log4j-1.2.16.jar:lib/commons-lang3-3.1.jar:lib/commons-lang-2.3.jar:lib/commons-configuration-1.8.jar"
FILESERVER_MAIN="pa2.FileServer"
COLLECTOR_MAIN="pa2.Collector"
CLIENT_MAIN="pa2.Client"
DEFS="-Djava.rmi.server.codebase=http://localhost/classes/pa2.jar -Djava.rmi.server.hostname=localhost"

# start collector
./run_collector.sh

read -p "When collector has finished starting, hit [Enter]..."

# start file servers
./run_fileservers.sh 5

read -p "When file servers have finished starting, hit [Enter]..."

# start a client 
# (start additional clients using "./run_clients.sh" in another terminal window)
./run_client.sh
