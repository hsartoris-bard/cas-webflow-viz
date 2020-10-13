(ns cas-webflow-viz.graphviz
  (:require [clojure.data.json :as json]))

(defrecord Edge     [^long _gvid          ; id of this edge       (required)
                     ^long tail           ; _gvid of tail node    (required)
                     ^long head           ; _gvid of head node    (required)
                     ^String label])

(defrecord Metanode [^long _gvid          ; id of this subgraph   (required)
                     ^String name         ; name of this subgraph (required)
                     #^long subgraphs     ; array of _gvid of child subgraph
                     #^long nodes         ; array of _gvid of nodes in this subgraph
                     #^long edges])       ; array of _gvid of edges in this subgraph

(defrecord Graph    [^String name         ; name of this graph    (required)
                     ^boolean directed    ; directed/undirected   (required)
                     ^boolean strict      ; graph is strict       (required)
                     ^long _subgraph_cnt  ; number of subgraphs   (required)
                     #^Metanode objects   ; array of subgraphs followed by nodes
                     #^Edge edges])       ; array of edges


