
(ns notify.db.mdb
  (:require
    [monger.collection :as mc]
    [mount.core :refer [defstate]]
    [mlib.mongo :refer [connect disconnect]]
    [mlib.config :refer [conf]]))
;

(def USER :user)
(def NOTIFY_USER_QUEUE :notify_user)


(def WAIT :wait)
(def WORK :work)
(def DONE :done)

(comment
  :notify_user 
  {
    :_id "oid"
    :user_id "uid"
    :time "action timestamp"
    :state #{WAIT WORK DONE :?FAIL}
    :type  #{private_message forum_msg forum_topic}
    :data  {}}    ;; event data
  .)
;

(defn indexes [conn]
  (let [db (:db conn)]
    ;
    (mc/create-index db NOTIFY_USER_QUEUE (array-map :time 1))
    (mc/create-index db NOTIFY_USER_QUEUE (array-map :user_id 1 :time 1)))
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

(defn get-user [id]
  (mc/find-map-by-id (conn) USER id))
  
;;.
