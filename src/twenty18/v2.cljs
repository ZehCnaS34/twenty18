(ns twenty18.v2
  (:require [twenty18.ecs :as ecs]
            [twenty18.utils :as uti]
            [twenty18.events :as eve]))

(def origin {:x 0 :y 0})

(defn -+
  ([a] a)
  ([a b] {:x (+ (:x a) (:x b))
          :y (+ (:y a) (:y b))})
  ([a b & more]
   (reduce + (+ a b) more)))

(ecs/defcomp ::transform
  {:twenty18.events/mouse-move
   (fn [this payload]
     (println this payload))}
  {:update
   (fn [ent]
     (println (str "updated " (uti/deltatime) "ms ago")))})
