(defn taking-while [pred]
  (fn [step]
    (fn [r x]
      (if (pred x)
        (step r x)
        (reduced r)))))

(defn take-n [n]
  (fn [step]
    (let [counter (volatile! n)]
      (fn 
        ([] (step))
        ([r] (step r))
        ([r x]
         (let [n @counter]
           (if (zero? n)
             r
             (do (vswap! counter dec)
                 (step r x)))))))))


(def ccat
  (fn [step]
    (fn 
      ([r] r)
      ([r x]
       (apply step r x)))))
