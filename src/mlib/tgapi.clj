(ns mlib.tgapi
  (:import
   [java.net SocketTimeoutException]
   [java.net.http HttpTimeoutException])
  (:require
   [clojure.string :refer [escape]]
   [jsonista.core :refer [write-value-as-bytes read-value keyword-keys-object-mapper]]
   [hato.client :as http]
   [mlib.http.client :as http-client]
   ,))


(def TIMEOUT 5000)
(def RETRY   3)
(def DELAY   10)


(defonce ^:private http-client* (atom nil))


(defn set-http-client!
  "Installs the process-wide Telegram HTTP client."
  [client]
  (reset! http-client* client))


(defn- client []
  (or @http-client* (set-http-client! (http-client/make-http-client))))


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
      ::retry)
    (catch HttpTimeoutException _
      ::retry)))


(defn api 
  "{:apikey '...', :timeout 3000, :retry 3, :socks {:host '' :port 9999}}"
  [cfg method params] 
  (let [tout  (:timeout cfg TIMEOUT)
        rmax  (:retry cfg RETRY)
        data {:url (api-url (:apikey cfg) method)
              :method :post
              :http-client (client)
              :headers {"Content-Type" "application/json"}
              :body (write-value-as-bytes params)
              :timeout tout
              :throw-exceptions? false}]
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


(defn send-message [tgc chat text]
  (api tgc :sendMessage {:chat_id chat :text text :parse_mode "HTML"}))



(comment

  (def nfy (:notify notify.config/conf))
  (def tgc (assoc (:telegram nfy) :timeout 10000))
  
  (send-message tgc "@angara_photos" "test1")

  )