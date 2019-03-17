#!/bin/bash

export APPNAME='notify'
export VERSION='0.1'
export COMMIT=`git rev-parse HEAD`
export TIMESTAMP=`date -Isec`

export CLASSES="tmp/classes"

rm -r ${CLASES} && mkdir -p ${CLASSES}
BUILD_EDN="${CLASSES}/build.edn"

echo "" > ${BUILD_EDN}
echo "{">>${BUILD_EDN}
echo ":appname \"${APPNAME}\"">>${BUILD_EDN}
echo ":version \"${VERSION}\"">>${BUILD_EDN}
echo ":commit \"${COMMIT}\"">>${BUILD_EDN}
echo ":timestamp \"${TIMESTAMP}\"">>${BUILD_EDN}

clj -e "(set! *compile-path* \"${CLASSES}\") (compile 'notify.main)" \
  && clj -A:jar \
  || exit 1

echo "start command:"
echo "  CONFIG_EDN=pat/to/runtime.edn java -cp notify.jar notify.main"

#.
