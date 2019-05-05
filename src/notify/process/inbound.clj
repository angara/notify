
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
      [ REDIS_RECONNECT_DELAY
        EVENT_TYPE_PRIVATE_MESSAGE
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
  (debug "forum-topic:" (dissoc (:forum_topic event) :text))
  (firehose event))
;

(defn forum-moder [event]
  (debug "forum-moder:" event)
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
  (when event
    (condp = (:type event)
      C/EVENT_TYPE_FORUM_MSG       (-> event fixup-forum-msg forum-message)
      ;;
      C/EVENT_TYPE_FORUM_MESSAGE   (forum-message event)
      C/EVENT_TYPE_FORUM_TOPIC     (forum-topic   event)
      C/EVENT_TYPE_FORUM_MODER     (forum-moder   event)
      C/EVENT_TYPE_PRIVATE_MESSAGE (private-message event)
      C/EVENT_TYPE_USER_REGISTER   (user/register event)
      C/EVENT_TYPE_USER_LOGIN      (user/login    event)
      (do 
        (warn "unexpected event:" event)
        (firehose event)))))
;


(defn reconnect-redis [state']
  (try
    (when-let [r (:redis @state')]
      (swap! state' assoc :redis nil)
      (.close r))
    (let [redis (connect (-> conf :redis :url))]
      (swap! state' assoc :redis redis)
      redis)
    (catch Exception ex
      (warn "reconnect-redis: catch" (.getMessage ex)))))
;

(defn feeder-init [state']
  (debug "feeder-init")
  (when-not (reconnect-redis state')
    (throw (ex-info "redis connection failed" {}))))
;

(defn try-fetch-event [state']
  (try
    (fetch-event (:redis @state'))
    (catch Exception ex
      (warn "try-fetch-event:" (.getMessage ex))
      (Thread/sleep REDIS_RECONNECT_DELAY)
      (reconnect-redis state')
      nil)))
;

(defn feeder-step [state']
  (try
    (when-let [event (try-fetch-event state')]
      (dispatch event))
    (catch Exception ex
      (warn "feeder-step:" (.getMessage ex)))))
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
