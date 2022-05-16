.EXPORT_ALL_VARIABLES:
SHELL = bash

.PHONY: dev build clean version deploy

# # #

dev:
	bash -c "set -a && source .env && clj -M:dev:nrepl"

version:
	@clj -T:build print-version > ./VERSION && cat ./VERSION

build:
	@mkdir -p ./target/resources
	@clj -T:build uberjar

deploy:
	scp target/notify.jar angara:/app/notify/

clean:
	rm -rf ./target

#.
