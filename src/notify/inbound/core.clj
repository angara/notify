(ns notify.inbound.core
  (:require
    [mount.core :refer [defstate]]
    [mlib.thread :refer [start-loop stop-loop]]
    ;
    [notify.redis.core :as redis]))
;

(defn feeder-init [state']
  (swap! state' assoc :conn "conn" :n 0)
  (.println System/out (str @state')))
;

(defn feeder-step [state']
  (.println System/out (str "step:" (:n (swap! state' update-in [:n] inc))))
  (Thread/sleep 2000)
  (throw (Error. "error!")))
;

(defn feeder-cleanup [state ex]
  (.println System/out (str "cleanup:" state ex)))
;

(defstate feeder
  :start
    (start-loop feeder-init feeder-step feeder-cleanup)
  :stop
    (stop-loop feeder))

;;.
