#!/bin/bash

export APPNAME='notify'
export VERSION='0.1'
export COMMIT=`git rev-parse HEAD`
export TIMESTAMP=`date -Isec`

export CLASSES="tmp/classes"

mkdir -p ${CLASSES}
BUILD_PROPS="${CLASSES}/build.properties"

echo "appname=${APPNAME}"     >  ${BUILD_PROPS}
echo "version=${VERSION}"     >> ${BUILD_PROPS}
echo "commit=${COMMIT}"       >> ${BUILD_PROPS}
echo "timestamp=${TIMESTAMP}" >> ${BUILD_PROPS}

clj -e "(set! *compile-path* \"${CLASSES}\") (compile 'notify.main)" \
  && clj -A:jar \
  || exit 1

echo "start command:"
echo "    java -cp notify.jar notify.main pat/to/config.edn"

#.
