
(ns dev
  (:require
    [clojure.walk :refer [postwalk]]
    [mount.core :refer [start-with-args]]

    [mlib.config :refer [conf]]

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

(def conf1 
  {"${1}" 
    { :a "a"
      :b "${2}"}})


;
(comment
  (run)

  clojure.core/*compile-path*

  (postwalk (fn [v] (prn v) v) conf1)

  (start-with-args {:a :b})

  (prn "conf:" conf)

  :.)
;

;;.

