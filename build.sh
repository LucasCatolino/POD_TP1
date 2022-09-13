#!/bin/bash

mvn clean install

cd ./client/target

tar xvzf POD_TP1-client-1.0-SNAPSHOT-bin.tar.gz

cd POD_TP1-client-1.0-SNAPSHOT

chmod -R +x *.sh

cd ../../../server/target

tar xvzf POD_TP1-server-1.0-SNAPSHOT-bin.tar.gz

cd POD_TP1-server-1.0-SNAPSHOT

chmod -R +x *.sh