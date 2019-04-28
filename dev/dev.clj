
(ns dev
  (:require
    [clojure.walk :refer [postwalk]]
    [mount.core :as mount]

    [honeysql.helpers :as h]

    [mlib.config :refer [conf]]
    [mlib.util :refer [edn-read edn-resource]]
    [mlib.tgapi :as tgapi]
    [mlib.sql :refer [fetch fetch-one exec]]

    [notify.redis.core :as r]
    [notify.process.inbound :as in]

    [notify.db.sql :as sql]
    [notify.db.mdb :as mdb]

    [monger.collection :as mc]))
;


(defn configs []
  [ (edn-resource "config.edn")
    (edn-read "tmp/dev.edn")])
;

(defn start-conf[]
  (mount/stop)
  (-> 
    (mount/only [#'conf])
    ; (mount/except [#'foo/c]
    (mount/with-args (configs))
    (mount/start)))
;

(def SELECT_MAIL
  (->
    (h/select :*)
    (h/from :mail_msg)
    (h/where [:= :id :?id])))
;


(defn get-mail [id]
  (fetch-one
    SELECT_MAIL
    {:id id}))

(comment
  (get-mail 1010)

  (:atime (mdb/get-user "1"))

  .)

(defn restart[]
  (mount/stop)
;  (mount/start-with-args configs)

  (-> 
    ; (mount/only [#'conf #'in/feeder])
    (mount/except [#'in/feeder])
    (mount/with-args (configs))
    (mount/start)))
;

; (def R (r/connect (-> conf :redis :url)))


(comment

  (mount/only [#'in/feeder])

  (mount/only [#'mdb/mdb])
  (mount/only [#'notify.db.sql/ds])

  mdb/mdb

  (mdb/get-user "1")

  (start-conf)
  (restart)

  (first
    (mc/find (:db mdb/mdb) :user {:_id "1"}))

  (mc/find-map-by-id (:db mdb/mdb) :user "1")

  mlib.sql.core/ds*

  conf
 
  notify.db.sql/ds

  (try
    (let [cfg (:notify conf)]
      (tgapi/send-message (:telegram cfg)
        (-> cfg :firehose :channel)
        ;-111
        "test of telegram api"))
    (catch Exception ex
      (ex-data ex)))

  
  (let [redis (r/connect (-> conf :redis :url))]
    (->
      (r/fetch-event redis)
      (in/dispatch)))
;

  .)
;

;;   "https://github.com/ztellman/manifold"

;;.

