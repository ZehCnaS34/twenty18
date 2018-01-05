(ns twenty18.v2
  (:require [twenty18.ecs :as ecs]
            [twenty18.utils :as uti]
            [twenty18.events :as eve]))

(def origin {:x 0 :y 0})

(defn keys-match [m & kys]
  (= (into #{} (select-keys m kys)) (into #{} kys)))

(defmulti box
  (fn [ds]
    (cond
      (keys-match ds #{:x :y :height :width}) :std
      :default :default
      )))

(defmethod box [{:keys [x y height width]}]
  {:x x
   :y y
   :x1 x
   :y1 y
   :x2 (+ x width)
   :y2 (+ y height)
   :width width
   :height height})

(defn -+
  ([a] a)
  ([a b] {:x (+ (:x a) (:x b))
          :y (+ (:y a) (:y b))})
  ([a b & more]
   (reduce + (+ a b) more)))

; emits :on-hit
(ecs/defcomp ::collider
  {:twenty18.events/update
   (fn [this])})

(ecs/defcomp ::transform
  {:twenty18.events/mouse-down
   (fn [this payload])

   :twenty18.events/mouse-up
   (fn [this payload])})
