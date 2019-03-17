
(ns notify.sender.telegram
  (:require
    [cheshire.core :refer [generate-string]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug info warn]]
    [mlib.tgapi :refer [esc send-message]]))
;

(defn firehose [event]
  (let [cfg  (-> conf :notify :telegram)
        chan (-> conf :notify :firehose :channel)
        text (generate-string event {:pretty true})
        ;; XXX: text length !!!
        html (str "<pre>" (esc text) "</pre>")]
    (try
      (send-message cfg chan html)
      (catch Exception ex
        (warn "firehose:" (ex-data ex))))))
;
