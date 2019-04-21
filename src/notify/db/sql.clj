
(ns notify.db.sql  
  (:require
    [mount.core :refer [defstate]]
    [hikari-cp.core :refer [make-datasource]]
    [mlib.config :refer [conf]]
    [mlib.sql :refer [set-ds]]))
;

; https://github.com/tomekw/hikari-cp/
; (make-datasource
;   {:connection-timeout 30000
;    :idle-timeout 600000
;    :max-lifetime 1800000
;    :minimum-idle 10
;    :maximum-pool-size  10
;    :adapter "postgresql"
;    :username "username"
;    :password "password"
;    :database-name "database"
;    :server-name "localhost"
;    :port-number 5432})

(defstate ds
  :start
    (->
      (:psql conf)
      (make-datasource)
      (set-ds))

  :stop
    (.close ds))
;

;;.
