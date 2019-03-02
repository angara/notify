
(ns notify.main
  (:require
    [clojure.string :refer [blank? split]]
    [mount.core     :refer [start-with-args]]
    [mlib.config    :refer [conf]]
    [mlib.util      :refer [edn-read edn-resource]]
    [notify.])
  (:gen-class))
;

(def APP "anga/notify")
  
(defn load-env-configs [env]
  (when env
    (->> (split env #"\:")
      (remove blank?)
      (map edn-read))))
;

(defn -main [& argv]
  (println APP "-" (.toString (java.util.Date.)))
  (start-with-args 
    (concat 
      [(edn-resource "config.edn") {:build (edn-resource "build.edn")}]
      (load-env-configs (System/getenv "CONFIG_EDN")))))
;

;;.
