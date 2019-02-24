
(ns dev
  (:require
    [notify.build :as build]
    [notify.redis.core :refer [hello]]))

(defn dev-props []
  (alter-var-root #'build/BUILD_PROPERTIES 
    (constantly 
      (delay 
        { :appname "notify-dev" 
          :version "dev-version"
          :timestamp "2019-mm-ddThh:mm:ss"
          :commit "xxxxxxxxxxxxx"}))))
;

(defn run []
  (dev-props)
  (prn "run props:" (build/props))
  (hello))
;

(comment
  (run)

  clojure.core/*compile-path*

  :.)
;

;;.

