#!/bin/bash

cd $(dirname "$0")
export CONFIG_EDN="../conf/angara.edn"
exec java -jar notify.jar >> /app/log/notify.log 2>&1

#.
