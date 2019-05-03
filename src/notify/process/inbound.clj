(ns notify.process.inbound
  (:require
    [clojure.stacktrace :refer [print-stack-trace]]
    [mount.core :refer [defstate]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug info warn]]
    [mlib.thread :refer [start-loop stop-loop]]
    [mlib.util :refer [now-ms]]
    ;
    [notify.const :as C :refer 
      [ EVENT_TYPE_PRIVATE_MESSAGE
        EVENT_TYPE_FORUM_MESSAGE
        USER_ONLINE_INTERVAL 
        ONLINE_USER_NOTIFY_DELAY_MS]]
    ;
    [notify.redis.core :refer [connect fetch-event]]
    [notify.db.mdb :refer [new-job]]
    [notify.db.sql :refer [topic-unread-watch]]
    [notify.process.user :as user]
    [notify.sender.telegram :refer [firehose]]))
;

;; ;; ;; ;; ;; private message ;; ;; ;; ;; ;;

(defn hide-text [s]
  (str "[Private text: " (count s) " chars]"))
;

(defn private-message [event]
  (firehose
    (update-in event [:private_message :text] hide-text))

  (let [msg (:private_message event)]
    (new-job 
      (now-ms)
      { :type EVENT_TYPE_PRIVATE_MESSAGE
        :mail_id (:id msg)
        :from_id (:user_id msg)
        :user_id (:to_id msg)})))
;

;; ;; ;; ;; ;; forum message ;; ;; ;; ;; ;;

(defn forum-message [event]
  (firehose event)

  (let [topic-id (get-in event [:forum_message :topic_id])
        subs (topic-unread-watch topic-id)]
    (doseq [s subs]
      (new-job 
        (now-ms)
        { :type EVENT_TYPE_FORUM_MESSAGE
          :topic_id topic-id
          :user_id (str (:uid s))}))))
;

(defn forum-topic [event]
  (debug "forum/topic:" event)
  (firehose event))
;

(defn forum-moder [event]
  (debug "forum/moder:" event)
  (firehose event))
;



;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn- fixup-forum-msg [event]
  (-> event
    (assoc 
      :type C/EVENT_TYPE_FORUM_MESSAGE 
      :forum_message (:forum_msg event))
    (dissoc :forum_msg)))
;

(defn dispatch [event]
  (condp = (:type event)
    C/EVENT_TYPE_FORUM_MSG       (-> event fixup-forum-msg forum/message)
    ;;
    C/EVENT_TYPE_FORUM_MESSAGE   (forum-message event)
    C/EVENT_TYPE_FORUM_TOPIC     (forum-topic   event)
    C/EVENT_TYPE_FORUM_MODER     (forum-moder   event)
    C/EVENT_TYPE_PRIVATE_MESSAGE (private-message event)
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
  ;;;
  (let [n (:n (swap! state' update-in [:n] (fnil inc 0)))]
    (debug "feeder-step:" n)
    (when (<= 200 n)
      (mlib.thread/clear-loop-flag state'))
    (Thread/sleep 100))
  ;;;
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
