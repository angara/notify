
(ns app.redis
  (:import 
    [java.util Date]
    [java.net URI]
    [redis.clients.jedis Jedis JedisPool JedisPoolConfig JedisPubSub]
    [org.json JSONObject])
    ;[anga Hello RedisEventQueue])
  (:require
    [cheshire.core :refer [generate-string parse-string]]))
;

(def EVENT_QUEUE      "event_queue")
(def EVENT_QUEUE_SEQ  "event_queue_seq")

;; (def redis-connect-timeout 8)
(def redis-block-timeout   4)

;; redis url: https://www.iana.org/assignments/uri-schemes/prov/redis
;; https://github.com/mmcgrana/clj-redis/blob/master/src/clj_redis/client.clj
;; https://github.com/ptaoussanis/carmine

(defn connect []
  (let [;uri (URI. (System/getenv "REDIS_ANGARA"))
        uri (URI. "redis://mdb:6379/1")]
    (Jedis. uri)))

(def R (connect))

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

; (defn get-event-seq [redis]
;   (.incr redis EVENT_QUEUE_SEQ))

(defn send-event [^Jedis redis ^String type data]
  (let [hdr { :eid (.incr redis EVENT_QUEUE_SEQ)
              :ts  (System/currentTimeMillis)
              :type type}
        body (generate-string (merge hdr data))]
    (.lpush redis EVENT_QUEUE 
      (into-array String [body]))))
;

(defn get-event [^Jedis redis]
  (let [[q body] (.brpop redis redis-block-timeout EVENT_QUEUE)]
    (when body
      (parse-string body keyword nil))))
;

(comment

  (defn lease [^JedisPool p f]
    (let [j (.getResource p)]
      (try
        (f j)
        (finally
          (.returnResource p j)))))



  (.set R EVENT_QUEUE_SEQ "1")
  (.incr R EVENT_QUEUE_SEQ)

  (.keys R "*")

  (.lrange R EVENT_QUEUE 0 -1)

  (.close R)

  (send-event R "msg" {:text "qwe 123"})

  (.brpop R redis-block-timeout EVENT_QUEUE)

  (get-event R)

  ; (let [[q v] (get-event R)]
  ;   (parse-string v keyword nil))
    
  (System/currentTimeMillis)
  (.getTime (Date.))

  (.lpush R EVENT_QUEUE 
    (doto (make-array String 2)
      (aset 0 "qwe")
      (aset 1 "123")))

  (.lpush R EVENT_QUEUE (into-array ["qwe"]))

  1)
;

(defn json-object []
  (doto (JSONObject.)
    (.put "eid" "123")                                                                                       
    (.put "ts" 12345678)
    (.put "type" "msg")
    (.put "msg"
          (doto (JSONObject.)
            (.put "text" "message text")))))
;

(comment
  (.toString
    (json-object))

  (let [r (RedisEventQueue. (System/getenv "REDIS_ANGARA") EVENT_QUEUE)
        j (doto (JSONObject.)
            (.put "text" "qwe 123"))]
    (.send r "test" j))

  1)
;
  ;; org.json builder

;; (def r (URL. "redis://user:pass@host:6379/db"))

