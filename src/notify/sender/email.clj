(ns notify.sender.email
  (:require
    [postal.core :refer [send-message]]    
    [taoensso.telemere :refer [log!]]
    [notify.config :refer [conf]]
  ))

(defn send-mail [to subj text]
  (let [smtp      (-> conf :sendmail :smtp)
        envelope  (-> conf :sendmail :envelope)
        rc  (send-message smtp
              (merge envelope
                { :type "text/plain;charset=utf-8"
                  :to [to]
                  :subject subj
                  :body text}))]
    ;
    (if (-> rc :error (= :SUCCESS))
      (log! ["send-mail.ok:" to subj])
      (log! :warn ["send-mail.failed:" to rc]))))

