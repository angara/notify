
(ns notify.main
  (:gen-class)
  (:require
    [notify.build :as build]
    [notify.redis.core :refer [hello]]))
;

(defn -main [& argv]
  (println "-main")
  (prn "main props:" (build/props))
  (hello))
;

;;.
