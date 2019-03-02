
(ns notify.redis.core
  (:require
    [mlib.config :refer [conf]]))
;

(defn hello []
  (prn "hello conf:" conf))
;

;;.
