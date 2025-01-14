(ns notify.db.sql  
  (:require
   [mount.core :refer [defstate args]]
   [hikari-cp.core :refer [make-datasource]]
   [honeysql.helpers :as h]
   [mlib.sql :refer [set-ds fetch fetch-one execute]]
   ,))


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
      (args)
      (:psql)
      (make-datasource)
      (set-ds))

  :stop
    (.close ds))


;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def FORUM_TOPICS   :forum_topics)
; (def FORUM_MESSAGES :forum_msgs)
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


(defn get-mail [id]
  (->
    (h/select :*)
    (h/from PRIVATE_MESSAGES)
    (h/where [:= :id id])
    (fetch-one)))


(defn mail-read? [mail]
  (:dest_time mail))


;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

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


(defn topic-unread-watch [topic-id]
  (fetch 
    SELECT_WATCHED_UNREAD 
    {:tid (Integer/parseInt topic-id)}))


;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def SELECT_USER_UNREAD_TOPIC
  (->
    (h/select :tp.tid :tp.title :tp.lastupdate)
    (h/from [FORUM_LASTREAD :lr])
    (h/join [FORUM_TOPICS :tp] 
      [:= :tp.tid :lr.tid])
    (h/where 
      [:= :lr.tid :?tid]
      [:= :lr.uid :?uid]
      [:> :lr.watch 0]
      [:> :tp.lastmsgid :lr.msgid])))


(defn user-unread-topic [user-id topic-id]
  (fetch-one
    SELECT_USER_UNREAD_TOPIC
    { :tid (Integer/parseInt topic-id)
      :uid (Integer/parseInt user-id)}))


; ; ; ; ; ; ; ; ; ;
;    state_vars
; ; ; ; ; ; ; ; ; ;
; 
; create table state_vars(vname varchar(80) primary key, ts timestamptz, i int8, s varchar);
;

(defn state-var-get [^String var-name]
  (->
   (h/select :i)
   (h/from :state_vars)
   (h/where [:= :vname :?var-name])
   (fetch-one {:var-name var-name})
   (:i)))


(defn state-var-put [^String var-name i]
  (-> [(str "insert into state_vars(vname,ts,i) values (?,CURRENT_TIMESTAMP,?)"
            "on conflict (vname) do update set ts=EXCLUDED.ts, i=EXCLUDED.i returning *")
       var-name i]
      (execute)
      (first)
      (:i)))


; NOTE: newer honey sql required
;
;; (defn state-var-inc [^String var-name i]
;;   (->
;;    (h/update :state_vars)
;;    (h/sset {:ts (sql/raw "CURRENT_TIMESTAMP") 
;;              :i [:+ :i :?i]})
;;    (h/where [:= :vname :?var-name])
;;    (exec {:var-name var-name :i i})
;;    (first)
;;    (:i)))


(defn state-var-gets [^String var-name]
  (->
   (h/select :s)
   (h/from :state_vars)
   (h/where [:= :vname :?var-name])
   (fetch-one {:var-name var-name})
   (:s)))


(defn state-var-puts [^String var-name ^String s]
  (-> [(str "insert into state_vars(vname,ts,s) values (?,CURRENT_TIMESTAMP,?)"
            "on conflict (vname) do update set ts=EXCLUDED.ts, s=EXCLUDED.s returning *")
       var-name s]
      (execute)
      (first)
      (:s)))


(comment

  (state-var-get "none")
  ;;=> nil
  
  (state-var-put "one" 1)
  ;;=> 1
  
  (state-var-get "one")
  ;;=> 1
  
  (state-var-puts "str1" "One")
  ;;=> "One"
  (state-var-gets "str1")
  ;;=> "One"

  )
