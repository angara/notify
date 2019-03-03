
(ns notify.redis.core
  (:import 
    [java.net URI]
    ;; https://github.com/xetorthio/jedis
    [redis.clients.jedis Jedis])
  (:require
    [cheshire.core :refer [parse-string]]
    [notify.const :refer [EVENT_QUEUE]]))
;


;; seconds to block on read queue
(def redis-blockread-timeout 4)


(defn connect [uri]
  (Jedis. (URI. uri)))
;

(defn fetch-event [^Jedis redis]
  (let [[q body] (.brpop redis redis-blockread-timeout EVENT_QUEUE)]
    (when body
      (parse-string body keyword nil))))
;


(comment

  (defn get-event-seq [redis]
    (.incr redis EVENT_QUEUE_SEQ))
  ;

  (defn send-event [^Jedis redis ^String type data]
    (let [hdr { :eid (.incr redis EVENT_QUEUE_SEQ)
                :ts  (System/currentTimeMillis)
                :type type}
          body (generate-string (merge hdr data))]
      (.lpush redis EVENT_QUEUE 
        (into-array String [body]))))
  ;

  .)

;;.
