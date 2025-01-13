(ns mlib.tgapi
  (:import 
    [java.net SocketTimeoutException])
  (:require
   [clojure.string :refer [escape]]
   [jsonista.core :refer [write-value-as-string read-value keyword-keys-object-mapper]]
   [org.httpkit.client :as http]
   ,))


(def TIMEOUT 5000)
(def RETRY   3)
(def DELAY   10)


(defn api-url [token method]
  (str "https://api.telegram.org/bot" token "/" (name method)))


(defn esc [text]
  (escape (str text) {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;"}))


(defn try-request [data]
  (try
    (let [{:keys [status body error]} (http/request data)]
      (case status
        200 (:result (read-value body keyword-keys-object-mapper))
        303 ::retry
        500 ::retry
        (let [res (read-value body keyword-keys-object-mapper)]
          (throw (ex-info (str "tgapi: status " status) res error)))))
    (catch SocketTimeoutException _
      ::retry)))


(defn api 
  "{:apikey '...', :timeout 3000, :retry 3, :socks {:host '' :port 9999}}"
  [cfg method params] 
  (let [tout  (:timeout cfg TIMEOUT)
        rmax  (:retry cfg RETRY)
        data {:url (api-url (:apikey cfg) method)
              :method :post
              :headers {"Content-Type" "application/json"}
              :body (write-value-as-string params)
              :connect-timeout tout
              :timeout tout}]
    (loop [retry rmax]
      (if (< 0 retry)
        (let [_ (Thread/sleep DELAY)    ;; calc proper delay
              res (try-request data)]
          (if (= res ::retry)
            (recur (dec retry))
            res))
        (throw
         (ex-info (str "tgapi - retry limit reached: " rmax) 
                  {:message (str "tgapi - retry limit reached: " rmax)}))))))


(defn send-message [cfg chat text]
  (api cfg :sendMessage {:chat_id chat :text text :parse_mode "HTML"}))
