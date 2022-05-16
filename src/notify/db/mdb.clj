
(ns notify.db.mdb
  (:require
    [java-time :as time]
    [monger.collection :as mc]
    [monger.query :as mq]
    [mount.core :refer [defstate]]
    [mlib.mongo :refer [connect disconnect id_id]]
    [mlib.config :refer [conf]]
    [mlib.util :refer [now-ms]])
  (:import 
    org.bson.types.ObjectId))  
;

(def USER :user)
(def NOTIFY_USER_QUEUE "notify_user")


(def WAIT    "wait")
(def PROCESS "process")


;; notify-type
;;
(def NFT_PRIVATE_MESSAGE :private_mesage)
(def NFT_FORUM_MESSAGE   :forum_message)
(def NFT_FORUM_TOPIC     :forum_topic)


(comment
  :notify_user 
  {
    :_id "str-oid"
    :inst "<int> action timestamp"
    :state #{WAIT PROCESS}    ;; ?DONE ?FAIL
    ;
    ;; :user_id "uid"
    ;; :type  #{private_message forum_message forum_topic}
    ;; :other values;
    }
  ,)
;

(defn indexes [conn]
  (let [db (:db conn)]
    ;
    (mc/create-index db NOTIFY_USER_QUEUE (array-map :inst 1)))
    ; (mc/create-index db NOTIFY_USER_QUEUE (array-map :user_id 1 :time 1)))
    ;
  conn)
;

(defstate mdb
  :start
    (-> conf
      (get-in [:mdb-angara :url])
      (connect)
      (indexes))
  :stop
    (disconnect mdb))
;

(defn conn []
  (:db mdb))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn get-user [id]
  (mc/find-map-by-id (conn) USER id))
;  

(defn user-online? [id active-duration]
  (let [atime (time/minus (time/local-date-time) active-duration)]
    (mc/find-one-as-map (conn) USER 
      { :_id id 
        :atime {:$gte atime}}
      [:_id :atime])))
;  


(comment

  (let [atime (time/minus (time/local-date-time) (time/seconds 100))]
    (mc/find-one-as-map (conn) USER 
      { :_id "1"
        :atime {:$gte atime}}
      [:_id :atime]))

  ,)

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn new-job [time-ms job-data]
  (id_id
    (mc/insert-and-return (conn) NOTIFY_USER_QUEUE
      (assoc job-data
        :_id (str (ObjectId.)) 
        :ct (now-ms)
        :inst time-ms
        :status WAIT))))
;

(defn peek-job [query]
  (-> (conn)
    #_{:clj-kondo/ignore [:invalid-arity]}
    (mq/with-collection (name NOTIFY_USER_QUEUE)
      (mq/find (assoc query :status WAIT))
      (mq/sort {:inst 1})
      (mq/limit 1))
    (first)
    (id_id)))
;

(defn select-job [query]
  (id_id
    (mc/find-and-modify (conn) NOTIFY_USER_QUEUE
      (assoc query :status WAIT)
      {:$set {:status PROCESS :ts (now-ms)}}
      {:sort {:inst 1} :return-new true})))
;

(defn finish-job [id]
  (mc/remove-by-id (conn) NOTIFY_USER_QUEUE id))
;
