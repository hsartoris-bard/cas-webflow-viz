#!/bin/bash
#_(
   DEPS='
   {:deps {macroz/tangle {:mvn/version "0.2.2"}
           org.clojure/data.json {:mvn/version "1.0.0"}
           com.rpl/specter {:mvn/version "1.1.3"}}}
   '
   exec clojure -Sdeps "$DEPS" -m cas-webflow-viz.tangle "$@"
)

(ns cas-webflow-viz.tangle
  (:require [tangle.core :as tangle]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.pprint :as pp]
            [com.rpl.specter :refer :all]))

(def debug true)

(defprotocol Edges (all-edges [_]))

(defn transition->edge [state-name]
  (when debug (println state-name))
  (fn [[label target]] [state-name target {:label  label}]))

(defrecord State [name transitions end? view? redirect?]
  Edges (all-edges [_] (mapv (transition->edge name) transitions)))

;(def extract-edges (map #(mapv keyword (clojure.string/split % #" -> "))))

(defn- parse-state [[state-name 
                    {:keys [transitions isEndState isViewState isRedirect]}]]
  (let [edges (map #(clojure.string/split % #" -> ") transitions)
        s (->State state-name edges isEndState isViewState isRedirect)]
    (when debug (pp/pprint edges))
    (when debug (pp/pprint s))
    {state-name (->State state-name edges isEndState isViewState isRedirect)}))

(defn- parse-edges [transitions]
  (mapv #(clojure.string/split % #" -> ") transitions))

(defn- parse-subflow [{states :states :as all}]
  (mapv (fn [[k v]] {k (repeat 5 k)}) all))
  ;(mapv parse-state states))
  ;(reduce-kv #(assoc %1 %2 (parse-state %2 %3)) {} states))

(defn- parse-webflow [webflow]
  ;(reduce-kv #(assoc %1 %2 (parse-subflow %3)) {} webflow))
  (transform [MAP-VALS :states MAP-VALS :transitions] parse-edges webflow))

(defn- get-edges [webflow]
  (->> webflow
       (select [MAP-VALS :states ALL (collect-one FIRST) LAST :transitions])
       (mapcat (fn [[k v]] (map (transition->edge k) v)))))


(defn- get-color [{:keys [end? view? redirect?]} default]
  (cond
    end?  :grey
    view? :turquoise
    :always default))

;(defn state->node [{:keys [name end? view?] :as state}]
(defn state->node [state-name {:keys [isEndState isViewState isRedirect]}]
  {:id        state-name
   :shape     :circle
   :color     :black
   :fillcolor (get-color {:end? isEndState :view? isViewState} :black)
   :fontcolor (if isEndState :white :black)
   :style     (if (or isEndState isViewState) :filled :empty)
   :penwidth  3})

(defn- get-nodes [webflow]
  (->> webflow
       (select [MAP-VALS :states ALL (collect-one FIRST) LAST])
       (map (fn [[k v]] (state->node k v)))))

(def tangle-opts
  {:directed? true
   :node->id #(if (keyword? %) (name %) (:id %))
   :node->descriptor identity})


(defn graph-webflow [webflow]
  (for [[subflow states] webflow]
    (let [nodes (map state->node states)
          edges (mapcat all-edges states)
          dot   (tangle/graph->dot nodes edges tangle-opts)
          basename (name subflow)]
      (clojure.pprint/pprint nodes)
      (spit (str basename ".dot") dot)
      (-> dot
          (tangle/dot->image "png")
          (io/copy (io/file (str basename ".png")))))))

;(defn parse-webflow 
;  ([webflow] (parse-webflow webflow {:node {:shape :circle} :directed? true}))
;  ([webflow graph-opts]
;   (when debug (clojure.pprint/pprint webflow))
;   (doseq [[subflow {states :states}] webflow]
;     (when debug (println subflow))
;     (let [nodes (keys states)
;           edges (mapcat get-edges states)]
;       (when debug 
;         (println "---------- NODES ----------")
;         (doseq [n nodes] (printf "\t%s\n" n))
;         (println "---------- EDGES ----------")
;         (doseq [[n t {tr :label}] edges] (printf "\t%s ---%s---> %s\n" n tr t)))
;       (-> (tangle/graph->dot nodes edges graph-opts)
;           (tangle/dot->image "png")
;           (io/copy (io/file (str subflow ".png"))))))))

(defn load-json [json-file]
  (-> (slurp json-file) (json/read-str :key-fn keyword)))
  ;(-> json-file slurp json/read-str))

(def webflow (load-json "webflow.json"))

;(when debug
;  (def webflow (load-json "webflow.json"))
;  (def mfa-gauth (webflow "mfa-gauth"))
;  (def gauth-states (mfa-gauth "states"))
;  (def parsed (parse-webflow webflow)))

(defn -main [json-file]
  (-> json-file
      load-json
      parse-webflow
      graph-webflow
      clojure.pprint/pprint)
  (shutdown-agents))
