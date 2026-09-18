(ns notify.main
  (:gen-class)
  (:require
   [mount.core :as mount]
   [taoensso.telemere :refer [log!]]
   [mlib.thread :refer [join]]
   [mlib.tgapi :as tg]
   [mlib.http.client :as http-client]
   [notify.config :as cfg]
   [notify.process.inbound]
   [notify.process.worker :refer [queue-worker]]
   [notify.forum.task]
   ))


(defn -main []
  (log! ["init:" (cfg/build-info)])
  (try
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(mount/stop)))
    (let [config (cfg/deep-merge (cfg/base-config) (cfg/env-config))
          proxy-url (some-> config :notify :telegram :http-proxy)]
      (tg/set-http-client! (http-client/make-http-client {:proxy-url proxy-url}))
      (-> config
          (mount/start-with-args)
          (as-> $ (log! ["started:" (str (:started $))]))))
    (let [rc (join queue-worker)]
      (log! ["stop..." rc]))
    (catch Exception ex
      (log! {:level :warn :error ex :msg "exception in main"})
      (Thread/sleep 2000)
      (System/exit 1))
    ,))
