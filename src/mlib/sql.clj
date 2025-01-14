(ns mlib.sql
  (:require
   [next.jdbc :as jdbc]
   [next.jdbc.date-time :refer [read-as-local]]
   [next.jdbc.result-set :refer [as-unqualified-maps]]
   [honeysql.core :as sql]
   ,))


(defonce ds* (atom nil))


(defn set-ds [ds]
  (read-as-local)
  (reset! ds* ds))


(defn get-conn []
  (jdbc/get-connection @ds*))


(defn fetch [sqlmap & [params]]
  (with-open [conn (get-conn)]
    (jdbc/execute! conn
                   (sql/format sqlmap :params params)
                   {:builder-fn as-unqualified-maps})))


(defn fetch-one [sqlmap & [params]]
  (first
    (with-open [conn (get-conn)]
      (jdbc/execute! conn
        (sql/format sqlmap :params params)
                     {:builder-fn as-unqualified-maps}
                     ))))


(defn exec [sqlmap & [params]]
  (with-open [conn (get-conn)]
    (jdbc/execute! conn
      (sql/format sqlmap :params params)
                   {:builder-fn as-unqualified-maps}
                   )))


(defn execute [sql-vec]
  (with-open [conn (get-conn)]
    (jdbc/execute! conn sql-vec
                   {:builder-fn as-unqualified-maps}
                   )))

