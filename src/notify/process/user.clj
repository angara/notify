(ns notify.process.user
  (:require
   [taoensso.telemere :refer [log!]]
   [notify.sender.telegram :refer [firehose]]
   ,))


(defn register [event]
  (log! ["user/register:" event])
  (firehose event))


(defn login [event]
  (log! ["user/login:" event])
  (firehose event))
