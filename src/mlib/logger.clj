
(ns mlib.logger
  (:import 
    [java.time ZonedDateTime]
    [java.time.format DateTimeFormatter]))
;    

;; TODO: configurable formatter, appenders

(defn timestamp []
  ;; https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
  (.format
    DateTimeFormatter/ISO_LOCAL_DATE_TIME
    (. ZonedDateTime now)))
;

(defn format-item [logger & args]
  (str (timestamp) " " (:name logger) " " (apply pr-str args)))
;

(defn stdout-handler [logger & args]
  (.println System/out (apply format-item logger args)))  

(defn stderr-handler [logger & args]
  (.println System/err (apply format-item logger args)))  
    
(def level-handler-map 
  {
    :debug stdout-handler
    :info  stdout-handler
    :warn  stderr-handler
    :error stderr-handler})
;

(defonce logger-map (atom {}))

(defn register! [level file line]
  (let [id (keyword (gensym "logger_"))]
    (swap! logger-map assoc id 
      { :level level 
        :file file 
        :line line 
        :name (str (.toUpperCase (name level)) ":" file "." line)
        :handler (get level-handler-map level stderr-handler)})
    id))
;

(defn log! [id & args]
  (let [logger (get @logger-map id)
        handler (:handler logger)]  
    (if handler
      (apply handler logger args)
      (.println *err* 
        (apply str (str "!!! missing logger: " id " !!! ") args)))))
;

(defmacro debug [& args]
  (let [id (register! :debug (str *ns*) (:line (meta &form)))]
    `(log! ~id ~@args)))
;

(defmacro info [& args]
  (let [id (register! :info (str *ns*) (:line (meta &form)))]
    `(log! ~id ~@args)))
;

(defmacro warn [& args]
  (let [id (register! :warn (str *ns*) (:line (meta &form)))]
    `(log! ~id ~@args)))
;

(defmacro error [& args]
  (let [id (register! :error (str *ns*) (:line (meta &form)))]
    `(log! ~id ~@args)))
;

(comment

  (debug "message" {:qwe 123})
  ;; 2019-03-17T12:13:23.793 DEBUG:mlib.logger.2 message {:qwe 123}

  .)

;;.
