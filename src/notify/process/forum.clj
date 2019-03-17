
(ns notify.process.forum
  (:require
    [mlib.logger :refer [debug info warn]]
    ;
    [notify.sender.telegram :refer [firehose]]))
;

(defn msg [event]
  (debug "forum/msg:" event)
  (firehose event))
;

(defn topic [event]
  (debug "forum/topic:" event)
  (firehose event))
;

(defn moder [event]
  (debug "forum/moder:" event)
  (firehose event))
;

;;.
