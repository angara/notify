
(ns dev
  (:require
    [clojure.walk :refer [postwalk]]
    [mount.core :as mount]
    [mlib.config :refer [conf]]
    [mlib.util :refer [edn-read edn-resource]]

    [notify.redis.core :as r]
    [notify.inbound.core :as inb]

    [notify.telegram.tgapi :as tgapi]))
;


(defn configs []
  [ (edn-resource "config.edn")
    (edn-read "tmp/dev.edn")])
;

(defn start-conf[]
  (mount/stop)
  (-> 
    (mount/only [#'conf])
    ; (mountexcept [#'foo/c]
    (mount/with-args (configs))
    (mount/start)))
;

(defn restart[]
  (mount/stop)
;  (mount/start-with-args configs)

  (-> 
    (mount/only [#'conf #'inb/feeder])
    ; (mountexcept [#'foo/c]
    (mount/with-args configs)
    (mount/start)))
;

; (def R (r/connect (-> conf :redis :url)))

(comment
  (start-conf)
  (restart)

  conf

  (try
    (let [cfg (:notify conf)]
      (tgapi/send-message (:telegram cfg)
        (-> cfg :firehose :channel)
        ;-111
        "test of telegram api"))
    (catch Exception ex
      (ex-data ex)))

  (get-in conf [:redis :url]) 

  (r/fetch-event R)



  .)
;

;;   "https://github.com/ztellman/manifold"

;;.

