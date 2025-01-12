(ns notify.config
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [mount.core :refer [defstate args]]
   [java-time.api :as jt]
  ,))


(defn build-info []
  (-> "build-info.edn" (io/resource) (slurp) (edn/read-string)))


(defn- deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
              (merge-with deep-merge* old new)
              new))]
    (if (every? map? maps)
      (apply merge-with f maps)
      (last maps))))


(defn deep-merge [& maps]
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
    (apply merge-with deep-merge* maps)))


(defn base-config []
  (-> "config.edn" 
      (io/resource) (slurp) (edn/read-string) 
      (assoc :build-info (build-info))))


(defn env-config []
  (-> "CONFIG_EDN" 
      (System/getenv) (slurp) (edn/read-string)))


(defstate conf
  :start (args))


(defstate tz
  :start (jt/zone-id (:tz conf)))
