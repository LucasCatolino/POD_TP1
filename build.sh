#!/bin/bash

mvn clean install

cd ./client/target

tar xvzf tpe1-g12-client-1.0-SNAPSHOT-bin.tar.gz

cd tpe1-g12-client-1.0-SNAPSHOT

chmod -R +x *.sh

cd ../../../server/target

tar xvzf tpe1-g12-server-1.0-SNAPSHOT-bin.tar.gz

cd tpe1-g12-server-1.0-SNAPSHOT

chmod -R +x *.sh