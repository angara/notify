
(ns mlib.sql
  (:require
    [clojure.string :refer [join] :rename {join str-join}]
    [java-time]
    [jdbc.core :as jdbc]
    [jdbc.proto :refer [ISQLType ISQLResultSetReadColumn]]
    [honeysql.core :as sql]))
;

(defonce ds* (atom nil))

(defn set-ds [ds]
  (reset! ds* ds))

(defn get-conn []
  (jdbc/connection @ds*))
;


(defn fetch [sqlmap & [params]]
  (with-open [conn (get-conn)]
    (jdbc/fetch conn
      (sql/format sqlmap :params params))))
;

(defn fetch-one [sqlmap & [params]]
  (first
    (with-open [conn (get-conn)]
      (jdbc/fetch conn
        (sql/format sqlmap :params params)))))
;

(defn exec [sqlmap & [params]]
  (with-open [conn (get-conn)]
    (jdbc/execute conn
      (sql/format sqlmap :params params))))
;

(defn next-id [tbl]
  (let [sel-next (str "select nextval('" tbl "_id_seq')")]
    (:nextval
      (first
        (with-open [conn (get-conn)]
          (jdbc/fetch conn [sel-next]))))))
;

(defn insert-into [tbl data]
  (let [fld-pairs (seq data)]
    (= 1 (exec {:insert-into (keyword tbl)
                :columns (map first fld-pairs)
                :values [(map second fld-pairs)]}))))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(comment
  (def struct
    { :table "table_name"
      :fields
        [ :id [:serial :pkey]
          :name1 "varchar"
          "name2" :varchar
          :ts  [:timestamp :now :null]]
      :indexes
        ["ts"]
      :unique
        ["uniq,flds"]}))
;

(defn field-type [type]
  (let
    [ null-flag
        (atom false)
      shortcuts
        (fn [t]
          (case t
            :pkey      "primary key"
            :bool      "boolean"
            :str40     "varchar(40)"
            :str80     "varchar(80)"
            :timestamp "timestamp with time zone"
            :now       "default CURRENT_TIMESTAMP"
            :zero      "default 0"
            :true      "default 't'"
            :false     "default 'f'"
            :nullable  (do (reset! null-flag true) nil)
            :null      (do (reset! null-flag true) nil)
            (name t)))
        ;
      ft
        (cond
          (keyword? type)
          (name type)
          ;
          (string? type)
          type
          ;
          (vector? type)
          (str-join " " (for [t type] (shortcuts t)))
          ;
          :else (str type))]
    ;
    (str ft (when-not @null-flag " not null"))))
;

(defn create-table-sql [struct]
  (str
    "create table " (-> struct :table name) " ("
    (str-join ", "
      (for [[fld type] (partition 2 2 nil (:fields struct))]
        (str (name fld) " " (field-type type))))
    ")"))
;

(defn create-index-sql [modif table idx ord]
  (str "create " modif " index " table "_idx" (inc ord)
        " on " table "(" (name idx) ")"))

;;; ;;;

(defn create-table [struct]
  (with-open [conn (get-conn)]
    (jdbc/execute conn
      (create-table-sql struct))
    ;
    (let [ord (atom 0)
          next-ord #(swap! ord inc)
          tbl (-> struct :table name)]
      ;
      (doseq [idx (:unique struct)]
        (jdbc/execute conn
          (create-index-sql "unique" tbl idx (next-ord))))
      ;
      (doseq [idx (:indexes struct)]
        (jdbc/execute conn
          (create-index-sql "" tbl idx (next-ord)))))))
  ;
;

(defn drop-table [struct]
  (with-open [conn (get-conn)]
    (jdbc/execute conn (str "drop table " (:table struct)))))
;

;;; ;;; ;;; ;;;

; java.time.LocalDate     - java.sql.Date
; java.time.LocalDateTime - java.sql.Timestamp
; java.time.LocalTime     - java.sql.Time

(extend-protocol ISQLType
  ;
  java.time.LocalDate
  (as-sql-type [this conn]
    (java-time/sql-date this))
  (set-stmt-parameter! [this conn stmt index]
    (.setDate stmt index
      (java-time/sql-date this)))

  java.time.LocalTime
  (as-sql-type [this conn]
    (java-time/sql-time this))
  (set-stmt-parameter! [this conn stmt index]
    (.setTime stmt index
      (java-time/sql-time this)))

  java.time.LocalDateTime
  (as-sql-type [this conn]
    (java-time/sql-timestamp this))
  (set-stmt-parameter! [this conn stmt index]
    (.setTimestamp stmt index
      (java-time/sql-timestamp this))))
  ;
;

(extend-protocol ISQLResultSetReadColumn
  ;
  java.sql.Timestamp
  (from-sql-type [this conn metadata index]
    (java-time/local-date-time this))
  ;
  java.sql.Date
  (from-sql-type [this conn metadata index]
    (java-time/local-date this))
  ;
  java.sql.Time
  (from-sql-type [this conn metadata index]
    (java-time/local-time this)))
;

;;; ;;; ;;; ;;;

;;.
