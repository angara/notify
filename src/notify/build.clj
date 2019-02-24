(ns notify.build
  (:import
    [java.io Reader]
    [java.util Properties])
  (:require
    [clojure.java.io :as io]))
;

(defn load-props [file]
  (with-open [^Reader reader (-> file io/resource io/reader)]
    (let [props (java.util.Properties.)]
      (.load props reader)
      (into {} 
        (for [[k v] props] 
          [(keyword k) v])))))
;

(def BUILD_PROPERTIES (delay (load-props "build.properties")))

(defn props []
  @BUILD_PROPERTIES)
;
  
;;.
