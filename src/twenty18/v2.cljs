(ns twenty18.v2)

(def origin {:x 0 :y 0})

(defn -+
  ([a] a)
  ([a b] {:x (+ (:x a) (:x b))
          :y (+ (:y a) (:y b))})
  ([a b & more]
   (reduce + (+ a b) more)))
