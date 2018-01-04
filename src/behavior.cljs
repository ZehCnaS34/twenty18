(ns twenty18.behavior
  (:require [twenty18.ecs :refer [defcomp] :as ecs]
            [twenty18.utils :refer [nil-id]]))

(defcomp ::bounce
  {:twenty18.events/update
   (fn [entity payload {:keys [on-update]}]
     (let [new-entity (nil-id on-update entity)
           {:keys [position size]} new-entity
           [x y] position
           [width height] size]
       new-entity))

   :twenty18.events/init
   (fn [entity payload {:keys [init]}]
     (merge entity {::state on-init}))})
