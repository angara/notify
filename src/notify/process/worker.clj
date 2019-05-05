
(ns notify.process.worker
  (:require
    [monger.collection :as mc]
    [mount.core :refer [defstate]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug info warn]]
    [mlib.thread :refer [start-loop stop-loop]]
    [mlib.util :refer [now-ms to-int]]
    ;
    [notify.const :refer 
      [ WORKER_RETRY_DELAY
        EVENT_TYPE_FORUM_MESSAGE 
        EVENT_TYPE_PRIVATE_MESSAGE
        USER_ONLINE_INTERVAL 
        ONLINE_USER_NOTIFY_DELAY_MS]]
    ;
    [notify.db.mdb :refer [new-job peek-job select-job finish-job get-user user-online?]]
    [notify.db.sql :refer [user-unread-topic get-mail mail-read?]]
    [notify.sender.email :refer [send-mail]]))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def WORKER_SLEEP_MS 5000)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn send-private-notify [user from-user mail]
  (let [email (:email user)
        subject (str "Angara.Net: Личное сообщение")
        text (str 
                "Cообщение от пользователя: " (:username from-user) "\n\n"
                "- - -\n" (:body mail) "\n- - -\n\n"
                "https://angara.net/mail/\n\n")]
    ;
    (send-mail email subject text)))
;

(defn send-topic-notify [user topic]
  (let [email (:email user)
        subject (str "Angara.Net/forum: " (:title topic))
        text (str 
                "Новое сообщение в теме:\n\n" (:title topic) "\n\n"
                "https://angara.net/forum/t" (:tid topic) "?unread\n\n")]

    (send-mail email subject text)))
;

(defn forum-message [{user-id :user_id topic-id :topic_id :as job}]
  (when-let [topic (user-unread-topic user-id topic-id)]
    (if (user-online? user-id USER_ONLINE_INTERVAL)
      (new-job
        (+ (now-ms) ONLINE_USER_NOTIFY_DELAY_MS)
        (update job :deferred (fnil inc 0)))
      ;;
      (let [user (get-user user-id)]
        ;; NOTE: check notification type        
        (send-topic-notify user topic)))))
;

(defn private-message [{user-id :user_id from-id :from_id mail-id :mail_id :as job}]
  (if-let [mail (get-mail (to-int mail-id))]
    (when-not (mail-read? mail)
      (if (user-online? user-id USER_ONLINE_INTERVAL)
        (new-job
          (+ (now-ms) ONLINE_USER_NOTIFY_DELAY_MS)
          (update job :deferred (fnil inc 0)))
        ;;
        (let [user (get-user user-id)
              from (get-user from-id)]
          (send-private-notify user from mail))))
    ;;
    (warn "private-message: mail message missing" mail-id)))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;



(defn do-job [job]
  (try
    (condp = (:type job)
      EVENT_TYPE_FORUM_MESSAGE    (forum-message job)
      EVENT_TYPE_PRIVATE_MESSAGE  (private-message job)
      (do
        (warn "do-job: unexpected type" job)))
    (catch Exception ex
      (warn "do-job failed:" job (.getMessage ex)))
    (finally
      (finish-job (:id job)))))
;
    
;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn worker-init [state']
  (debug "worker-init")
  state')
;

(defn worker-step [state']
  (try
    (if-let [job (select-job {})]
      (do-job job)
      (let [next-job (peek-job {})
            sleep (- (now-ms) (or (:inst next-job) 0))]
        (Thread/sleep (min WORKER_SLEEP_MS (max 0 sleep)))))
    (catch Exception ex
      (warn "worker-step:" (.getMessage ex))
      (Thread/sleep WORKER_RETRY_DELAY))))
;

(defn worker-cleanup [state ex]
  (debug "worker-cleanup:" state ex))
;

(defstate queue-worker
  :start
    (start-loop worker-init worker-step worker-cleanup)
  :stop
    (stop-loop queue-worker))
;
  
;;.
