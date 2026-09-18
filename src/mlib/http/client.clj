(ns mlib.http.client
  "Reusable hato HttpClient construction with an optional HTTP proxy
  (and optional proxy authentication).

  A proxy URL may be any of:
    host:port
    http(s)://host:port
    http(s)://user:pass@host:port
  nil/blank means a direct connection."
  (:require
   [clojure.string :as str]
   [hato.client :as hc])
  (:import
   [java.net Authenticator Authenticator$RequestorType
    InetSocketAddress PasswordAuthentication ProxySelector
    URI URISyntaxException]))


(def ^:private default-connect-timeout 10000)


(defn- redact-proxy-url [proxy-url]
  (some-> proxy-url
          (str/replace #"(?i)^([a-z][a-z0-9+.-]*://)?([^/@:]+):([^/@]*)@"
                       "$1***:***@")))


(defn- parse-proxy
  "Parses a proxy URL into {:host :port :user :pass}.
  user/pass are optional. nil/blank is handled by make-http-client."
  [^String proxy]
  (let [invalid-proxy-ex (fn
                           ([]
                            (ex-info "invalid http proxy" {:proxy-url (redact-proxy-url proxy)}))
                           ([cause]
                            (ex-info "invalid http proxy" {:proxy-url (redact-proxy-url proxy)} cause)))
        uri (try
              (URI. (if (str/includes? proxy "://") proxy (str "http://" proxy)))
              (catch URISyntaxException ex
                (throw (invalid-proxy-ex ex))))
        host (.getHost uri)
        port (or (.getPort uri) 80)]
    (when (str/blank? host)
      (throw (invalid-proxy-ex)))
    (let [[user pass] (some-> (.getUserInfo uri) (str/split #":" 2))]
      {:host host :port port :user user :pass pass})))


(defn- proxy-selector [{:keys [host port]}]
  (ProxySelector/of (InetSocketAddress. host port)))


(defn- proxy-authenticator
  "Returns a PROXY-scoped Authenticator when both user and pass are present."
  [{:keys [user pass]}]
  (when (and user pass)
    (proxy [Authenticator] []
      (getPasswordAuthentication []
        (when (= (.getRequestorType this) Authenticator$RequestorType/PROXY)
          (PasswordAuthentication. user (char-array pass)))))))


(defn- allow-basic-proxy-tunneling! []
  ;; Some JDKs disable Basic proxy authentication for HTTPS CONNECT tunnels.
  (System/setProperty "jdk.http.auth.tunneling.disabledSchemes" ""))


(defn make-http-client
  "Builds a reusable hato HttpClient.

  :proxy-url supports host:port, http(s)://host:port and
  http(s)://user:pass@host:port."
  ([]
   (make-http-client {}))
  ([{:keys [proxy-url connect-timeout]
     :or {connect-timeout default-connect-timeout}}]
   (if (str/blank? proxy-url)
     (hc/build-http-client {:connect-timeout connect-timeout})
     (let [p    (parse-proxy proxy-url)
           auth (proxy-authenticator p)]
       (when auth
         (allow-basic-proxy-tunneling!))
       (hc/build-http-client
        (cond-> {:connect-timeout connect-timeout
                 :proxy           (proxy-selector p)}
          auth (assoc :authenticator auth)))))))
