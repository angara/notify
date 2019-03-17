
(ns notify.process.private
  (:require
    [mlib.logger :refer [debug info warn]]
    ;
    [notify.sender.telegram :refer [firehose]]))
;

(defn hide-text [s]
  (str "[Removed " (count s) " chars]"))
;

(defn mail [event]
  (debug "private/mail:" event)

  (firehose
    (update-in event [:private_message :text] hide-text)))
;

;;.
