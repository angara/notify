# Angara.Net: notification service

[DataFlow](./dataflow.md)

## Scratchpad

```sh
https://github.com/mmcgrana/clj-redis/blob/master/src/clj_redis/client.clj
https://github.com/ztellman/manifold
http://clojureelasticsearch.info/
```

```sh

# /etc/cloud/cloud.cfg:
# preserve_hostname: true
# manage_etc_hosts: false

ln -fs /usr/share/zoneinfo/Asia/Irkutsk /etc/localtime
dpkg-reconfigure --frontend noninteractive tzdata

apt install openjdk-8-jdk

curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash -
apt install -y nodejs
npm install -g pm2
pm2 startup ubuntu -u app --hp /app

# pm2 install pm2-logrotate

mkdir /app/conf
mkdir /app/notify
chown app:root /app/conf /app/notify
chmod o-rx /app/* /app/.pm2

# ./deploy.sh

cd /app/notify; pm2 start pm2.json; pm2 save

```
