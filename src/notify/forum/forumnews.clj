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

