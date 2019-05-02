
(ns mlib.mongo
  (:require
    [monger.core :as mg]
    [monger.collection :as mc]
    [monger.conversion :refer [ConvertFromDBObject ConvertToDBObject to-db-object]]) 
  (:import
    [java.time LocalDateTime ZonedDateTime ZoneId]
    [java.util Date]
    [org.bson.types ObjectId]
    [com.mongodb WriteConcern]))
;

;;; ;;; ;;; ;;; ;;;

(defn id_id [r]
  (if-let [id (:_id r)]
    (assoc (dissoc r :_id) :id id)
    r))
;

(defn new_id [] 
  (ObjectId.))

(defn oid [s]
  (try (ObjectId. s) 
    (catch Exception e 
      (str s))))

(defn next-sn [conn seq-coll seq-name]
  (-> conn
    (mc/find-and-modify 
      seq-coll
      {:_id (name seq-name)}
      {"$inc" {:n (int 1)}}
      {:return-new true :upsert true})
    (get :n)
    (long)))
;

;;; ;;; ;;; ;;; ;;;

(defn connect [url]
  (let [mdb (mg/connect-via-uri url)]
    (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
    mdb))
;

(defn disconnect [mdb]
  (mg/disconnect (:conn mdb)))
;

(defn get-conn [mdb]
  (:db mdb))

;;; ;;; ;;; ;;; ;;;

(extend-protocol ConvertToDBObject
  LocalDateTime
  (to-db-object [^LocalDateTime input]
    (to-db-object (Date/from (.toInstant (.atZone input (ZoneId/systemDefault))))))
  ZonedDateTime
  (to-db-object [^ZonedDateTime input]
    (to-db-object (Date/from (.toInstant input (ZoneId/systemDefault))))))

(extend-protocol ConvertFromDBObject
  java.util.Date
  (from-db-object [^java.util.Date input keywordize]
    (java.time.LocalDateTime/ofInstant (.toInstant input) (ZoneId/systemDefault))))
  

; (cheshire.generate/add-encoder ObjectId
;   (fn [^ObjectId oid ^com.fasterxml.jackson.core.json.WriterBasedJsonGenerator generator]
;     (.writeString generator (.toString oid))))
; (cheshire.generate/add-encoder BSONTimestamp
;   (fn [^BSONTimestamp ts ^com.fasterxml.jackson.core.json.WriterBasedJsonGenerator generator]
;     (cheshire.generate/encode-map {:time (.getTime ts) :inc (.getInc ts)} generator)))  

;;.
