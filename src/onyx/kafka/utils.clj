(ns onyx.kafka.utils
  (:require [clj-kafka.consumer.zk :as zkconsumer]
            [taoensso.timbre :refer [info] :as timbre]
            [clj-kafka.core :as zkcore]))

(defn take-segments
  "Reads segments from a topic until a :done is reached."
  [zk-addr topic decompress-fn]
  (let [kafka-config {"zookeeper.connect" zk-addr
                      "group.id" "onyx-consumer"
                      "auto.offset.reset" "smallest"
                      "auto.commit.enable" "false"}]
    (zkcore/with-resource [c (zkconsumer/consumer kafka-config)]
      zkconsumer/shutdown
      (conj (->> (zkconsumer/messages c topic)
                 (map :value)
                 (map decompress-fn)
                 (take-while (fn [v] (not= :done v)))
                 vec)
            :done))))
