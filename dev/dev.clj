
(ns dev
  (:require
    [java-time :as time]
    [clojure.walk :refer [postwalk]]
    [mount.core :as mount]

    [honeysql.helpers :as h]

    [mlib.config :refer [conf]]
    [mlib.util :refer [edn-read edn-resource]]
    [mlib.tgapi :as tgapi]
    [mlib.sql :refer [fetch fetch-one exec]]

    [notify.const :as const]
    [notify.redis.core :as r]
    [notify.process.inbound :as in]
    [notify.process.worker :as wr]

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

(defn restart[]
  (mount/stop)
  (-> 
    (mount/except [#'in/feeder #'wr/queue-worker])
    (mount/with-args (configs))
    (mount/start)))
;

; (def R (r/connect (-> conf :redis :url)))


(comment
  (restart)

  (sql/get-mail 1012)

  (sql/topic-unread-watch "18896")

  (:atime (mdb/get-user "1"))


  (let [uid "1"]
        ;atime (time/minus (time/local-date-time) (time/seconds 200))]
    (mdb/user-online? uid const/USER_ONLINE_INTERVAL))

  (mount/only [#'in/feeder])

  
  (mount/only [#'mdb/mdb])
  (mount/only [#'notify.db.sql/ds])

  (mount/stop [#'in/feeder])
  (mount/start [#'in/feeder])

  (mount/stop [#'wr/queue-worker])
  (mount/start [#'wr/queue-worker])

  mdb/mdb

  (mdb/peek-job {})

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

