
(ns notify.db.mdb
  (:require
    [monger.collection :as mc]
    [mount.core :refer [defstate]]
    [mlib.mongo :refer [connect disconnect]]
    [mlib.config :refer [conf]]))
;

(def USER :user)


(defstate mdb
  :start
    (-> conf
      (get-in [:mdb-angara :url])
      (connect))
  :stop
    (disconnect mdb))
;

(defn conn []
  (:db mdb))
;

(defn get-user [id]
  (mc/find-map-by-id (conn) USER id))

;;.
