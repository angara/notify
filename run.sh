#!/bin/bash

cd $(dirname "$0")
export CONFIG_EDN="../conf/angara.edn"
exec java -cp notify.jar notify.main >> /app/log/notify.log 2>&1

#.
