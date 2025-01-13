
# # #

APP_NAME   = angara-notify
VER_MAJOR  = 1
VER_MINOR  = 2
MAIN_CLASS = notify.main

JAR_NAME   = notify.jar
UBER_JAR   = target/${JAR_NAME}

# # #

SHELL = bash
.EXPORT_ALL_VARIABLES:
.PHONY: dev build clean version deploy


all: clean build deploy # restart

dev:
	bash -c "set -a && source .env && clj -M:dev:nrepl"

build:
	@mkdir -p ./target/resources
	@clj -T:build uberjar

run:
	CONFIG_EDN=../conf/dev.edn java -jar ${UBER_JAR}

deploy:
	scp ${UBER_JAR} angara:/app/notify/

clean:
	@clojure -T:build clean

outdated:
	@clojure -Sdeps '{:deps {com.github.liquidz/antq {:mvn/version "2.11.1264"}}}' -M -m antq.core

#.
