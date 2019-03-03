
(ns dev
  (:require
    [clojure.walk :refer [postwalk]]
    [mount.core :as mount]
    [mlib.config :refer [conf]]
    [notify.redis.core :refer [hello]]))
;


(def conf1 
  {"${1}" 
    { :a "a"
      :b "${2}"}})

(defn restart[]
  (mount/stop)
  (mount/start))
;

(comment

  (restart)

  conf 
  *out*


  (mount/start-with-args [])


  .)
;

;;.

