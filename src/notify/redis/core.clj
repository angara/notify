(ns notify.redis.core
  (:import 
    [java.net URI]
    ;; https://github.com/xetorthio/jedis
    [redis.clients.jedis Jedis])
  (:require
   [jsonista.core :refer [write-value-as-string read-value keyword-keys-object-mapper]]
   [notify.const :refer [REDIS_CONNECT_TIMEOUT EVENT_QUEUE EVENT_QUEUE_SEQ]]
   ,))


;; seconds to block on read queue
(def redis-blockread-timeout 4)


(defn connect [uri]
  (Jedis. (URI. uri) REDIS_CONNECT_TIMEOUT))


(defn fetch-event [^Jedis redis]
  (let [[_q body] (.brpop redis redis-blockread-timeout EVENT_QUEUE)]
    (when body
      (read-value body keyword-keys-object-mapper)
      ,)))


(defn get-event-seq [redis]
  (.incr redis EVENT_QUEUE_SEQ))


(defn send-event [^Jedis redis ^String type data]
  (let [hdr { :eid (.incr redis EVENT_QUEUE_SEQ)
              :ts  (System/currentTimeMillis)
              :type type}
        body (write-value-as-string (merge hdr data))]
    (.lpush redis EVENT_QUEUE 
      (into-array String [body]))))
