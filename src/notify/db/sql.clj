
(ns notify.db.sql  
  (:require
    [mount.core :refer [defstate]]
    [hikari-cp.core :refer [make-datasource]]
    [honeysql.helpers :as h]
    [mlib.config :refer [conf]]
    [mlib.sql :refer [set-ds fetch fetch-one]]))
;

; https://github.com/tomekw/hikari-cp/
; (make-datasource
;   {:connection-timeout 30000
;    :idle-timeout 600000
;    :max-lifetime 1800000
;    :minimum-idle 10
;    :maximum-pool-size  10
;    :adapter "postgresql"
;    :username "username"
;    :password "password"
;    :database-name "database"
;    :server-name "localhost"
;    :port-number 5432})

(defstate ds
  :start
    (->
      (:psql conf)
      (make-datasource)
      (set-ds))

  :stop
    (.close ds))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def FORUM_TOPICS   :forum_topics)
(def FORUM_MESSAGES :forum_msgs)
(def FORUM_LASTREAD :forum_lastread)

(def PRIVATE_MESSAGES :mail_msg)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;
(comment
  :forum_lastread
    ; uid            | integer                     |           | not null | 
    ; tid            | integer                     |           | not null | 
    ; msgid          | integer                     |           | not null | 
    ; watch          | integer                     |           | not null | 0
    ; post_count     | integer                     |           | not null | 0)
  :forum_topics)
    ; tid        | integer                     |           | not null | nextval('forum_topics_tid_seq'::regclass)
    ; tgroup     | integer                     |           | not null | 
    ; owner      | integer                     |           | not null | 
    ; title      | character varying(400)      |           | not null | 
    ; created    | timestamp without time zone |           | not null | ('now'::text)::timestamp(6) with time zone
    ; lastupdate | timestamp without time zone |           | not null | ('now'::text)::timestamp(6) with time zone
    ; lastposter | integer                     |           |          | 
    ; closed     | boolean                     |           | not null | false
    ; msgnum     | integer                     |           | not null | 0
    ; lastmsgid  | integer                     |           | not null | 0
    ; ordi       | integer                     |           | not null | 0)

;


(defn get-mail [id]
  (->
    (h/select :*)
    (h/from PRIVATE_MESSAGES)
    (h/where [:= :id id])
    (fetch-one)))
;
    
(def SELECT_WATCHED_UNREAD
  (->
    (h/select :lr.uid :lr.tid :lr.msgid)
    (h/from [FORUM_LASTREAD :lr])
    (h/join [FORUM_TOPICS :tp] 
      [:= :tp.tid :lr.tid])
    (h/where 
      [:= :tp.tid :?tid]
      [:> :lr.watch 0]
      [:> :tp.lastmsgid :lr.msgid])))
;

(defn topic-unread-watch [topic-id]
  (fetch 
    SELECT_WATCHED_UNREAD 
    {:tid (Integer/parseInt topic-id)}))
;

;;.
