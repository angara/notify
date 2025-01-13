(ns notify.forum.task
  (:require
   [clojure.core.async :refer [thread <!!]]
   [taoensso.telemere :refer [log!]]
   [mount.core :refer [defstate]]
   [notify.config :refer [conf]]
   [notify.forum.forumnews :refer [forum-task]]
   [notify.forum.forumphotos :refer [forum-photos]]
   ))


(defn now-ms []
  (System/currentTimeMillis))


(defn start-periodical-task [interval task-fn]
  (let [runflag (atom true)]
    {:runflag runflag
     :thread  (thread
                (loop [t0 0]
                  (task-fn)
                  (when @runflag
                    (let [dt (- interval (- (now-ms) t0))]
                      (when (> dt 0)
                        (Thread/sleep dt)))
                    (recur (now-ms)))))}))


(defn stop-periodical-task [process]
  (when process
    (reset! (:runflag process) false)
    (<!! (:thread process))))


(defstate forumnews
  :start
  (if-let [cfg (-> conf :notify :forumnews)]
    (do
      (log! ["forumnews start:" cfg])
      (start-periodical-task
       (* (:fetch-interval cfg) 1000)
       #(forum-task (-> conf :notify :telegram) cfg)))
    (do
      (log! :warn ["forumnews did not start"])
      false))
  :stop
  (stop-periodical-task forumnews))


(defstate forumphotos
  :start
  (if-let [cfg (-> conf :notify :forumphotos)]
    (do
      (log! ["forumphotos start:" cfg])
      (start-periodical-task
       (* (:fetch-interval cfg) 1000)
       #(forum-photos (-> conf :notify :telegram) cfg)))
    (do
      (log! :warn ["forumphoto did not start"])
      false))
  :stop
  (stop-periodical-task forumphotos))
