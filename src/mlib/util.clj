;;
;;  mlib.util
;;

(ns mlib.util
  (:require
   [clojure.string :as str]
   ))


(defn to-int
  "returns nil or default on failure"
  ( [s]
    (to-int s nil))
  ( [s default]
    (try
      (if (string? s) (Long/parseLong s) (long s))
      (catch Exception _ default))))



(defn now-ms []
  (System/currentTimeMillis))


(defn hesc [text]
  (str/escape text {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;"}))


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
