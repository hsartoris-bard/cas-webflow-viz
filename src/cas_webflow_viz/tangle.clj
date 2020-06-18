#!/bin/bash
#_(
   DEPS='
   {:deps {macroz/tangle {:mvn/version "0.2.2"}}}
   '
   exec clojure -Sdeps "$DEPS" -m cas-webflow-viz.tangle "$@"
)

(ns cas-webflow-viz.tangle
  (:require [tangle.core :as tangle]
            [clojure.java.io :as io]))
(def html-node 
  {:id "html" 
   :color "blue" 
   :label [:TABLE 
           {:BORDER 0}
           [:TR 
            [:TD "hic"]
            [:TD {:BORDER 1} "cup"]]]})

(def nodes [:a :b :c :d html-node])

(def edges [[:a :b]
            [:a :c]
            [:c :d]
            [:a :c {:label "another"
                    :style :dashed}]
            [:a :html]])

(def dot (tangle/graph->dot 
           nodes 
           edges 
           {:node {:shape :box}
            :node->id (fn [n] (if (keyword? n) (name n) (:id n)))
            :node->descriptor (fn [n] (when-not (keyword? n) n))}))

(defn -main []
  (println "Running main func")
  (io/copy (tangle/dot->image dot "png") (io/file "hello.png"))
  (println "Finished graph stuff")
  (shutdown-agents))
