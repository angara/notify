(ns notify.const
  (:require
    [java-time :as time]))
  
;; global constants

;; redis keys:

(def EVENT_QUEUE      "event_queue")
(def EVENT_QUEUE_SEQ  "event_queue_seq")

(def DEFERRED_QUEUE   "notify_deferred_queue")
(def USER_QUEUE       "notify_user:")           ;; "notify_user:<uid>"

(def EVENT_TYPE_FORUM_TOPIC       "forum_topic")
(def EVENT_TYPE_FORUM_MESSAGE     "forum_message")
(def EVENT_TYPE_FORUM_MODER       "forum_moder")
(def EVENT_TYPE_PRIVATE_MESSAGE   "private_message")
;; deprecated
(def EVENT_TYPE_FORUM_MSG         "forum_msg")


(def EVENT_TYPE_USER_REGISTER     "user_register")
(def EVENT_TYPE_USER_LOGIN        "user_login")


(def USER_ONLINE_INTERVAL (time/seconds 200))
(def ONLINE_USER_NOTIFY_DELAY_MS (* 240 1000))

;;.
