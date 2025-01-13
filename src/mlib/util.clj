;;
;;  mlib.util
;;

(ns mlib.util
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]))


(defn to-int
  "returns nil or default on failure"
  ( [s]
    (to-int s nil))
  ( [s default]
    (try
      (if (string? s) (Long/parseLong s) (long s))
      (catch Exception _ default))))




;; ;; ;; time ;; ;; ;;

(defn now-ms []
  (System/currentTimeMillis))


;; ;; ;; edn ;; ;; ;;

(defn edn-read [file]
  (edn/read-string (slurp file)))


(defn edn-resource [res]
  (-> res io/resource slurp edn/read-string))


;; ;; ;; deep merge ;; ;; ;;

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
