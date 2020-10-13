(def completions (keys (ns-publics (find-ns 'clojure.core))))

(with-open [f (-> (System/getenv "HOME") 
                  (str "/.clj_completions")
                  (java.io.FileWriter.)
                  (java.io.BufferedWriter.))]
  (.write f (apply str (interpose \newline completions))))

