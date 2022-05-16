
(ns notify.main
  (:gen-class)
  (:require
    [clojure.java.io :as io]
    [clojure.string :refer [blank? split trim]]
    [mount.core     :refer [defstate start-with-args]]
    [taoensso.timbre  :refer [debug info warn] :as timbre]
    ;;
    ;; [mlib.config    :refer [conf]]
    [mlib.util      :refer [edn-read]]
    [mlib.thread    :refer [join]]
    ;;
    [notify.process.inbound]
    [notify.process.worker :refer [queue-worker]]
  ))


(def build-info
  (delay (-> "build-info" (io/resource) (slurp) (trim))))

  
(defn load-env-configs [env]
  (when env
    (->> (split env #"\:")
      (remove blank?)
      (map edn-read))))
;

(defstate app-start
  :start
    (info "started."))
;

(defn -main [& _]
  
  (timbre/merge-config!
     {:output-fn  (partial timbre/default-output-fn {:stacktrace-fonts {}})
      :min-level  [[#{"notify.*"} :debug]
                   [#{"*"} :info]]})
  
  (info (str "init: " @build-info))

  (start-with-args
    (load-env-configs (System/getenv "CONFIG_EDN")))
  ;;
  (info "stop..." 
    (join queue-worker)))
;
