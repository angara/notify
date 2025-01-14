(ns notify.forum.forumnews
  (:require
   [clojure.string :as s]
   [taoensso.telemere :refer [log!]]
   [java-time.api :as jt]
   [honeysql.helpers :as hs]
   [mlib.sql :refer [fetch]]
   [mlib.tgapi :as tg]
   [mlib.util :refer [hesc]]
   [notify.db.sql :refer [state-var-get state-var-put]]
   ))


(def FORUMNEWS_LAST_TID "forumnews.last_tid")


(def FETCH-TOPICS 
  (->
    (hs/select :*)
    (hs/from :forum_topics)
    (hs/where 
      [:> :tid :?last-tid] 
      [:>= :created :?last-time])
      ;; [:< :tgroup 1000]
    (hs/order-by [:tid :asc])
    (hs/limit :?limit)))


(defn fetch-topics [last-tid age-limit fetch-limit]
  (fetch FETCH-TOPICS 
    {
      :last-tid  last-tid 
      :last-time (jt/minus (jt/instant) (jt/seconds age-limit))
      :limit     fetch-limit}))

(comment
  (fetch-topics 1 100000 10)
  ;;=> [{:tid 127731,
  ;;     :closed false,
  ;;     :lastupdate #object[java.time.LocalDateTime 0x3bac5fa1 "2025-01-14T14:31:39.708780"],
  ;;     :msgnum 5,
  ;;     :tgroup 70,
  ;;     :created #object[java.time.LocalDateTime 0x2261f3fc "2025-01-14T10:52:27.436018"],
  ;;     :lastmsgid 1469195,
  ;;     :title "Состояние лыжни Переезд-Ангасолка",
  ;;     :ordi 0,
  ;;     :lastposter 12508,
  ;;     :owner 53137}]

  ;; { "_id" : "forumnews", "last_tid" : 127731, "ts" : ISODate("2025-01-14T02:59:44.676Z") }
  ;; { "_id" : "forumphotos", "last_mid" : 1469201, "ts" : ISODate("2025-01-14T12:59:50.294Z") }

  (state-var-put FORUMNEWS_LAST_TID 127731)

  (state-var-put "forumphotos.last_msgid" 1469201)
  
  )

(defn update-channel [tgc channel topics]
  (Thread/sleep 30)
  (let [fmt (fn [t] (str "<a href=\"https://angara.net/forum/t" 
                         (:tid t) "\">" (hesc (:title t)) "</a>"))
        txs (map fmt (reverse topics))]
    (tg/send-message tgc channel {:text (str "-\n" (s/join "\n\n" txs))
                                     :parse_mode "HTML"
                                     :disable_web_page_preview true})))


(defn forum-task [tgc cfg]
  (try
    (let [chn (:channel cfg)
          tpm (:topics-per-message cfg)
          last-tid (state-var-get FORUMNEWS_LAST_TID)
          topics (not-empty (fetch-topics last-tid (:age-limit cfg) (:fetch-limit cfg)))]
      (when topics
        (let [max-tid (reduce #(max %1 (:tid %2)) last-tid topics)]
          (when (not= last-tid max-tid)
            (state-var-put FORUMNEWS_LAST_TID max-tid))
          (doseq [tps (partition tpm tpm nil topics)]
            (doseq [t tps]
              (log! ["forumnews: new topic -" (:tid t) (:title t)]))
            (update-channel tgc chn tps)))))
    (catch Exception ex
      (log! {:level :warn :error ex :msg ["forum-task:" ex]}))))

