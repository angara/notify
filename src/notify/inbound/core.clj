(ns notify.inbound.core
  (:require
    [mount.core :refer [defstate]]))
;

(defn start-feeder [])

(defstate feeder
  :start
    (start-feeder))

;;.

