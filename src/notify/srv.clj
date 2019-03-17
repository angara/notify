
(ns notify.srv
  (:require
    [mount.core :refer [defstate]]
    [mlib.config :refer [conf]]
    [notify.redis.core :refer [connect]]))
;


(defn feeder-init [state']
  (let [redis (connect (-> conf :redis :url))]
    (swap! state' assoc :redis redis)))
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
;

;;.
