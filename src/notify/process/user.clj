
(ns notify.process.user
  (:require
    [taoensso.timbre  :refer [debug info warn]]
    ;
    [notify.sender.telegram :refer [firehose]]))
;

(defn register [event]
  (debug "user/register:" event)
  (firehose event))
;

(defn login [event]
  (debug "user/login:" event)
  (firehose event))
;
