(ns mlib.tgapi
  (:import 
    [java.net SocketTimeoutException])
  (:require
    [clojure.string :refer [escape]]
    [cheshire.core :refer [parse-string]]
    [clj-http.client :as http]
    [clj-http.conn-mgr :refer [make-socks-proxied-conn-manager]]))
;

(def TIMEOUT 5000)
(def RETRY   3)
(def DELAY   10)

(defn api-url [token method]
  (str "https://api.telegram.org/bot" token "/" (name method)))
;

(defn esc [text]
  (escape (str text) {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;"}))
;

(defn api-try [url data]
  (try
    (let [{:keys [status body]} (http/post url data)]
      (case status
        200 (:result (parse-string body true))
        303 ::retry
        500 ::retry
        (let [res (parse-string body true)]
          (throw (ex-info (str "tgapi: status " status) res)))))
    (catch SocketTimeoutException _
      ::retry)))
;

(defn api 
  "{:apikey '...', :timeout 3000, :retry 3, :socks {:host '' :port 9999}}"
  [cfg method params] 
  (let [tout  (:timeout cfg TIMEOUT)
        rmax  (:retry   cfg RETRY)
        cmgr  (when-let [socks (:socks cfg)]
                (make-socks-proxied-conn-manager (:host socks) (:port socks)))
        url   (api-url (:apikey cfg) method)
        data { :content-type :json
               :form-params params
               :throw-exceptions false
               :connection-manager cmgr
               :socket-timeout tout
               :conn-timeout tout}]
    (loop [retry rmax]
      (if (< 0 retry)
        (let [_ (Thread/sleep DELAY)    ;; calc proper delay
              res (api-try url data)]
          (if (= res ::retry)
            (recur (dec retry))
            res))
        ;;
        (throw
          (ex-info (str "tgapi - retry limit reached: " rmax) 
            {:message (str "tgapi - retry limit reached: " rmax)}))))))
;

(defn send-message [cfg chat text]
  (api cfg :sendMessage {:chat_id chat :text text :parse_mode "HTML"}))
;

;;.
