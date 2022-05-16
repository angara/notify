
(ns notify.sender.email
  (:require
    [postal.core :refer [send-message]]    
    [taoensso.timbre  :refer [debug info warn]]
    [mlib.config :refer [conf]]
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
      (info "send-mail.ok:" to subj)
      (warn "send-mail.failed:" to rc))))
;
