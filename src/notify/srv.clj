
(ns notify.srv
  (:require
    [mount.core :refer [defstate]]
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug info warn]]
    [notify.redis.core :refer [connect]]
    [notify.process.inbound :refer [feeder]]
    [notify.process.worker :refer [queue-worker]]))
;


; (defn feeder-init [state']
;   (let [redis (connect (-> conf :redis :url))]
;     (swap! state' assoc :redis redis)))
; ;

; (defn feeder-step [state']
;   (debug "feeder-step:" (:n (swap! state' update-in [:n] inc)))
;   (Thread/sleep 2000)
;   (throw (Error. "error!")))
; ;

; (defn feeder-cleanup [state ex]
;   (debug "feeder-cleanup:" state ex))
; ;

; (defstate feeder
;   :start
;     (start-loop feeder-init feeder-step feeder-cleanup)
;   :stop
;     (stop-loop feeder))
; ;

;;.
