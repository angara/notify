
(ns notify.redis.core
  (:require
    [notify.build :refer [props]]))
;

(defn hello []
  (prn "hello props:" (props)))
;

;;.
