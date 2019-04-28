
(ns notify.queue
  (:require
    [monger.collection :as mc]
    [notify.db.mdb :refer [mdb NOTIFY_USER_QUEUE]]))
;


(defn push [user-id event-type data]

  (mc/insert)
  
  (prn))
