(ns user
  (:require
   [portal.api :as portal]
   [mount.core :as mount]
   [notify.config :as cfg]
   [notify.main]
   ,))


;; (defn restart []
;;   (mount/stop)
;;   (mount/start))

;; (defn reset []
;;   (tnr/refresh :after 'user/restart))


(comment

  (def p (portal/open {:launcher :vs-code})) ;; NOTE: portal extension required
  (add-tap #'portal/submit)

  (portal/clear) ; Clear all values
  
  (remove-tap #'portal/submit) ; Remove portal from tap> targetset
  
  (portal/close) ; Close the inspector when done
  (portal/docs) ; View docs locally via Portal - jvm / node only
  
  
  ;; (require '[clojure.datafy :as d])
  ;; (require '[portal.api :as p])
  
  ;; (def submit (comp p/submit d/datafy))
  ;; (add-tap #'submit)
  ;; (tap> *ns*)
  
  (-> 
   (cfg/deep-merge (cfg/base-config) (cfg/env-config))
   (mount/with-args)
   (mount/only #{#'notify.config/conf})
   (mount/start)
   )

  (mount/stop)

  ;; (with-connection [conn dbc]
  ;;   (pg/query conn "select * from meteo_stations"))
  
  ()
  )
