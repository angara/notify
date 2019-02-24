;;
;;  Angara.Net: Notify
;;

(def project 
  { :name "angara.net/notify" 
    :version "0.1.0"})

(def jar-main 'notify.main)
(def jar-file "notify.jar")

(def dev-config "tmp/dev-config.edn")

(set-env!
  :source-paths   #{"src"}
  :resource-paths #{"resources"}
  :asset-paths    #{"resources"}

  ;; boot -d boot-deps ancient
  :dependencies
  '[
    [org.clojure/clojure "1.10.0"]

    [org.clojure/tools.logging "0.4.1"]
    [ch.qos.logback/logback-classic "1.2.3"]

    [clj-time "0.15.1"]
    [clj-http "3.9.1"]

    [ring/ring-core "1.7.1"]
    [ring/ring-jetty-adapter "1.7.1"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.3.0"]

    [cheshire "5.8.1"]
    [compojure "1.6.1"]

    [hiccup "1.0.5"]
    [garden "1.3.6"]
    [mount "0.1.16"]

    [com.novemberain/monger "3.5.0"]
    [org.postgresql/postgresql "42.2.5"]

    ;; https://funcool.github.io/clojure.jdbc/latest/
    [funcool/clojure.jdbc "0.9.0"]
    
    ;; https://github.com/tomekw/hikari-cp
    [hikari-cp "2.7.0"]
    
    ;; https://github.com/jkk/honeysql
    [honeysql "0.9.4"]  
    [com.draines/postal "2.0.3"]

    ;; dev
    ;;
    [org.clojure/tools.namespace "0.2.11" :scope "test"]])

    ;; https://github.com/martinklepsch/boot-garden
;    [org.martinklepsch/boot-garden "1.3.2-1" :scope "test"]
;

(require
  '[clojure.tools.namespace.repl :refer [set-refresh-dirs refresh]]
  '[clojure.edn :as edn]
  '[clj-time.core :as tc]
  '[mount.core :as mount]
  '[boot.git :refer [last-commit]])
;  '[org.martinklepsch.boot-garden :refer [garden]])
;

; (task-options!
;   garden
;   {
;     :styles-var 'css.root/main
;     :output-to  "public/incs/css/main.css"
;     :pretty-print false})
; ;

;;; ;;; ;;; ;;;

(defn start []
  (require jar-main)
  (-> dev-config
    (slurp)
    (edn/read-string)
    (mount/start-with-args)))
;

(defn go []
  (mount/stop)
  (apply set-refresh-dirs (get-env :source-paths))
  (refresh :after 'boot.user/start))
;

;;; ;;; ;;; ;;;

(defn increment-build []
  (let [bf "res/build.edn"
        num (-> bf slurp edn/read-string :num)
        bld { :timestamp (str (tc/now))
              :commit (last-commit)
              :num (inc num)}]
    (spit bf (.toString (merge project bld)))))
;

; (deftask css-dev []
;   (comp
;     (garden :pretty-print true)
;     (target :dir #{"tmp/res/"})))
; ;

(deftask test-env []
  (set-env! :source-paths #(conj % "test"))
  identity)
;

(deftask dev []
  (set-env! :source-paths #(conj % "test"))
  (apply set-refresh-dirs (get-env :source-paths))
  ;;
  (create-ns 'user)
  (intern 'user 'reset
    (fn []
      (prn "(user/reset)")
      ((resolve 'boot.user/go))))
  ;;
  identity)
;

(deftask build []
  (increment-build)
  (comp
    ;; (javac)
    ;; (garden)
    (aot :all true)
    (uber)
    (jar :main jar-main :file jar-file)
    (target :dir #{"tmp/target"})))
;

;;.
