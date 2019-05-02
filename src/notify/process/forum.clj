
(ns notify.process.forum
  (:require
    [mlib.logger :refer [debug info warn]]
    [mlib.util :refer [now-ms]]
    ;
    [notify.const :refer 
      [ USER_ONLINE_INTERVAL ONLINE_USER_NOTIFY_DELAY_MS
        EVENT_TYPE_FORUM_MESSAGE]]
    [notify.db.mdb :refer [new-job get-user user-online?]]
    [notify.db.sql :refer [topic-unread-watch]]
    [notify.sender.telegram :refer [firehose]]))
;

(defn message [event]
  (debug "forum/message:" event)
  (firehose event)

  (let [topic-id (get-in event [:forum_message :topic_id])
        subs (topic-unread-watch topic-id)]
    (doseq [s subs]
      (debug "=============== s:" s)
      (let [uid (str (:uid s))
            is-online (user-online? uid USER_ONLINE_INTERVAL)
            inst  (if is-online 
                    (+ (now-ms) ONLINE_USER_NOTIFY_DELAY_MS)
                    (now-ms))
            data {:type EVENT_TYPE_FORUM_MESSAGE
                  :user_id uid
                  :topic_id topic-id}]
        (new-job inst data)))))
;

(defn topic [event]
  (debug "forum/topic:" event)
  (firehose event))
;

(defn moder [event]
  (debug "forum/moder:" event)
  (firehose event))
;

;;.
