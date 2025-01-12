(ns notify.main
  (:gen-class)
  (:require
   [mount.core :refer [start-with-args]]
   [taoensso.telemere :refer [log!]]
   [mlib.thread :refer [join]]
   [notify.config :as cfg]
   [notify.process.inbound]
   [notify.process.worker :refer [queue-worker]]
   ))


(defn -main []
  (log! ["init:" (cfg/build-info)])
  (try
    (->
     (cfg/deep-merge (cfg/base-config) (cfg/env-config))
     (start-with-args)
     (as-> $
           (log! ["started:" (str (:started $))])))
    (let [rc (join queue-worker)]
      (log! ["stop..." rc]))
    (catch Exception ex
      (log! {:level :warn
             :error ex
             :msg "exception in main"}))
    ,))
