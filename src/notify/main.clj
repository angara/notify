
(ns notify.main
  (:gen-class)
  (:require
    [clojure.string :refer [blank? split]]
    [mount.core     :refer [defstate start-with-args]]
    ;;
    [mlib.config    :refer [conf]]
    [mlib.util      :refer [edn-read edn-resource]]
    [mlib.logger    :refer [info debug]]
    [mlib.thread    :refer [join]]
    ;;
    [notify.process.inbound :refer [feeder]]
    [notify.process.worker :refer [queue-worker]]))
;
  
(defn load-env-configs [env]
  (when env
    (->> (split env #"\:")
      (remove blank?)
      (map edn-read))))
;

(defstate app-start
  :start
    (info "started:" (:build conf)))
;

(defn -main [& argv]
  (info "init...")
  (start-with-args 
    (concat
      [(edn-resource "config.edn") {:build (edn-resource "build.edn")}]
      (load-env-configs (System/getenv "CONFIG_EDN"))))
  ;;
  (info "stop..." 
    (join queue-worker)))
;

;;.
