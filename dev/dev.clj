
(ns dev
  (:require
    [clojure.walk :refer [postwalk]]
    [mount.core :as mount]
    [mlib.config :refer [conf]]
    [mlib.util :refer [edn-read edn-resource]]

    [notify.redis.core :as r]))
;


(def configs
  [
    (edn-resource "config.edn")
    (edn-read "tmp/dev.edn")])
;

(defn restart[]
  (mount/stop)
  (mount/start-with-args configs))
;

(def R (r/connect (-> conf :redis :url)))

(comment

  (restart)

  (get-in conf [:redis :url]) 

  (r/fetch-event R)

  (mount/start-with-args [])

  "https://github.com/ztellman/manifold"

  .)
;

;;.

