
(ns notify.sender.telegram
  (:require
    ; [clojure.string :refer [subs]]
    [cheshire.core :refer [generate-string]]
    ;
    [mlib.config :refer [conf]]
    [mlib.logger :refer [debug info warn]]
    [mlib.tgapi :refer [esc send-message]]
    ;
    [notify.const :as C]))
;

(def NOTIFY_TEXT_MAX 2000)
(def NOTIFY_MSG_MAX 3000)

(def TELEGRAM_SEND_DELAY 20)

(defn truncate-text [s]
  (if (>= (.length s) NOTIFY_TEXT_MAX)
    (str (subs s 0 NOTIFY_TEXT_MAX) " ...")
    s))
;

(defn format-event [event]
  (let [type (:type event)
        body ((keyword type) event)
        uid  (:user_id body)
        eid  (str "<pre>eid:" (:eid event) "</pre>")
        hdr  (str "<b>" type "</b>" " 🔹 user:" uid "\n")
        title (esc (str (:title body)))
        text (esc (truncate-text (str (:text body))))
        attr (dissoc body :text :user_id :title)
        attr (esc (generate-string attr :pretty))]
    (if (< NOTIFY_MSG_MAX (+ (.length title) (.length text) (.length attr)))
      (str hdr "\n<b>Very long message truncated!</b>" eid)
      (str hdr 
        "<pre>" attr "</pre>\n\n" 
        (when (< 0 (.length title))
          (str "🔸 <b>" title "</b>\n"))
        text "\n\n" eid))))
;

(defn firehose [event]
  (let [cfg  (-> conf :notify :telegram)
        chan (-> conf :notify :firehose :channel)
        html (format-event event)]
    (try
      (Thread/sleep TELEGRAM_SEND_DELAY)
      (send-message cfg chan html)
      (catch Exception ex
        (warn "firehose:" (ex-data ex))))))
;

;;.
