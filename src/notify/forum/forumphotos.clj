(ns notify.forum.forumphotos
  (:require
   [clojure.string :as s]
   [java-time.api :as jt]
   [taoensso.telemere :refer [log!]]
   [honeysql.helpers :as hs]
   [mlib.sql :refer [fetch]]
   [mlib.tgapi :as tg]
   [notify.db.sql :refer [state-var-get state-var-put]]
   ))


(def FORUMPHOTOS_LAST_MSGID "forumphotos.last_msgid")


(def MSGS_PER_PAGE 20)

;; select msgid, topic, owner, updated, attach from forum_msgs 
;; where attach ilike 'jpg%' and not censored order by msgid;

(def FETCH-MSGS-JPG
  (->
    (hs/select 
      [:m.msgid :mid] [:m.topic :tid] [:m.owner :uid] [:m.updated :ts]
      [:t.title :title] 
      [:u.login :username])
    (hs/from [:forum_msgs :m]) 
    (hs/join [:forum_topics :t] [:= :m.topic :t.tid])
    (hs/left-join [:users :u] [:= :u.uid :m.owner])
    (hs/where 
      [:> :m.msgid :?last-mid]
      [:> :m.updated :?last-time]
      [:like :m.attach "jpg%"] 
      [:not= :m.censored true])
    (hs/order-by [:mid :asc])
    (hs/limit :?limit)))
    

(def TOPIC-MIDS
  (->
    (hs/select [:m.msgid :mid])
    (hs/from [:forum_msgs :m])
    (hs/where [:= :m.topic :?tid])
    (hs/order-by [:mid :asc])))


(defn fetch-msgs [last-mid age-limit fetch-limit]
  (fetch FETCH-MSGS-JPG
    {
      :last-mid  last-mid 
      :last-time (jt/minus (jt/instant) (jt/seconds age-limit))
      :limit     fetch-limit}))


(defn msg-page [msg]
  (let [mid (:mid msg)
        mids (->> (fetch TOPIC-MIDS {:tid (:tid msg)}) (map :mid))
        idx  (first (keep-indexed (fn [i v] (when (= mid v) i)) mids))]
    (if idx
      (assoc msg :p (quot idx MSGS_PER_PAGE))
      msg)))


(defn base-uri [mid]
  (let [d0 (-> mid (mod 10000) (quot 100))
        d1 (-> mid (mod 100))]
    (format "/upload/%02d/%02d/f_%d.jpg" d0 d1 mid)))
  

(defn word-trunc [text len & [ellip]]
  (if (<= (.length text) len)
    text
    (let [ellip (or ellip "...")
          len (- len (.length ellip))
          idx (s/last-index-of text " " len)]
      (str (.substring text 0 (or idx len)) ellip))))


(defn update-channel [tgc channel msg]
  (Thread/sleep 30)
  (log! ["msg:" msg])
  (let [photo-url (str "http://angara.net" (base-uri (:mid msg)))
        msg-url   (str "http://angara.net/forum/t" (:tid msg) 
                       (when-let [p (:p msg)] (str "?p=" p)) "#" (:mid msg))
        username (:username msg)
        ll (str "\n\n" username " - " msg-url)
        txt (str "Из темы:\n" (:title msg))
        txt (str (word-trunc txt (- 199 (.length ll))) ll)]
    (tg/api tgc :sendPhoto {:chat_id channel :photo photo-url :caption txt})))


(defn forum-photos [tgc cfg]
  (try
    (let [chn (:channel cfg)
          last-mid (state-var-get FORUMPHOTOS_LAST_MSGID)
          msgs (not-empty (fetch-msgs last-mid (:age-limit cfg) (:fetch-limit cfg)))
          msgs (map msg-page msgs)]
      (when msgs
        (let [max-mid (reduce #(max %1 (or (:mid %2) 0)) last-mid msgs)]
          (state-var-put FORUMPHOTOS_LAST_MSGID max-mid)
          (doseq [m msgs]
            (update-channel tgc chn m)))))
    (catch Exception ex
      (log! {:level :warn :error ex :msg ["forum-photos:" ex]}))
    ,))
