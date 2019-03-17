(ns notify.process.inbound
  (:require
    [clojure.stacktrace :refer [print-stack-trace]]
    [mount.core :refer [defstate]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug info warn]]
    [mlib.thread :refer [start-loop stop-loop]]
    ;
    [notify.const :as C]
    [notify.redis.core :refer [connect fetch-event]]
    [notify.process.forum :as forum]
    [notify.process.private :as private]
    [notify.process.user :as user]
    [notify.sender.telegram :refer [firehose]]))
;

(defn dispatch [event]
  (condp = (:type event)
    C/EVENT_TYPE_FORUM_MSG       (forum/msg     event)
    C/EVENT_TYPE_FORUM_TOPIC     (forum/topic   event)
    C/EVENT_TYPE_FORUM_MODER     (forum/moder   event)
    C/EVENT_TYPE_PRIVATE_MESSAGE (private/mail  event)
    C/EVENT_TYPE_USER_REGISTER   (user/register event)
    C/EVENT_TYPE_USER_LOGIN      (user/login    event)
    (do 
      (warn "unexpected event:" event)
      (firehose event))))
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
      (mlib.thread/clear-loop-flag state'))
    (Thread/sleep 1000))
  ;
  (->
    (:redis @state')
    (fetch-event)
    (dispatch)))
  
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
