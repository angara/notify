#!/bin/bash

scp tmp/notify.jar pm2.json run.sh app:/app/notify/
ssh app pm2 restart notify

#.
