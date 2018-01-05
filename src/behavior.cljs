(ns twenty18.behavior
  (:require [twenty18.ecs :refer [defcomp] :as ecs]
            [twenty18.utils :refer [nil-id]]
            [twenty18.dom :refer [view-size]]))

(defn collided? [b1 b2]
  (let [x2 (+ (x) width)
        y2 (+ y height)
        bounding (view-size)]
    ))

(defn edged? [inner outer]
  (let [x1 (:x1 inner)
        x2 (:x2 inner)
        y1 (:y1 inner)
        y2 (:y2 inner)
        xx1 (:x1 outer)
        xx2 (:x2 outer)
        yy1 (:y1 outer)
        yy2 (:y2 outer)]
    (or (>= y2 (:height bounding))
        (>= x2 (:width bounding))
        (<= x 0)
        (<= y 0))))

(defn hit-wall [{:keys [x y width height] :as box}]
  (if (edged? box (view-size))))

(defn new-position [entity]
  (let [{:keys [position size]} entity
        state (::state entity)
        {:keys [speed x-factor y-factor]} state]
    ))

(defcomp ::bounce
  {:twenty18.events/update
   (fn [entity payload {:keys [on-update]}]
     (let [new-entity (nil-id on-update entity)
           {:keys [position size]} new-entity
           [x y] position
           [width height] size
           box (merge position size)]
       (new-position new-entity)
       new-entity))

   :twenty18.events/init
   (fn [entity payload {:keys [init]}]
     (merge entity {::state init}))})
