
(ns notify.process.private
  (:require
    [mlib.logger :refer [debug info warn]]
    ;
    [notify.sender.telegram :refer [firehose]]))
;

(defn hide-text [s]
  (str "[Private text: " (count s) " chars]"))
;

(defn mail [event]
  (debug "private/mail:" event)

  (firehose
    (update-in event [:private_message :text] hide-text)))

  
  ; (let [user (get-user )])
  ; if user.email
  ; if allow notify
  ; it atime < now + 5 min => ts + delay 5 min | now
  ; push to notify_user:id <ts>
  
;

;;.
