(ns notify.inbound.core
  (:require
    [clojure.stacktrace :refer [print-stack-trace]]
    [mount.core :refer [defstate]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug info warn]]
    [mlib.thread :refer [start-loop stop-loop]]
    ;
    [notify.redis.core :refer [connect fetch-event]]))
;

(defn feeder-init [state']
  (debug "feeder-init")
  (let [redis (connect (-> conf :redis :url))]
    (swap! state' assoc :redis redis)))
;

(defn feeder-step [state']
  (let [n (:n (swap! state' update-in [:n] (fnil inc 0)))]
    (debug "feeder-step:" n)
    (when (<= 10 n)
      (mlib.thread/clear-loop-flag state')))
  ;
  (let [event (fetch-event (:redis @state'))]
    (debug "event:" event)
    (Thread/sleep 2000)))
;

(defn feeder-cleanup [state ex]
  (debug "feeder-cleanup:" state ex))
;

(defstate feeder
  :start
    (start-loop feeder-init feeder-step feeder-cleanup)
  :stop
    (stop-loop feeder))
;

;;.
