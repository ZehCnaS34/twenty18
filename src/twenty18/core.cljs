(ns twenty18.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [goog.dom :as gdo]
            [goog.events :as gev]
            [twenty18.v2 :as v2]
            [twenty18.ecs :as ecs :refer [defent]]
            [twenty18.events :as eve]
            [twenty18.utils :refer [fps now deltatime update-clock nil-id]]
            [twenty18.dom :refer [canvas]]
            [twenty18.render]
            [twenty18.behavior]
            [om.next :as om :refer-macros [defui]]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [cljs.pprint :refer [pprint]]
            [om.dom :as dom]))

(enable-console-print!)

(def width (.-width canvas))
(def height (.-height canvas))

(defonce render-chan (chan))

(ecs/defcomp ::clickable
  {:twenty18.events/mouse-down
   (fn [this {:keys [position]} {:keys [on-down]}]
     (nil-id on-down this position))

   :twenty18.events/mouse-up
   (fn [this {:keys [position]} {:keys [on-up]}]
     (nil-id on-up this position))})

(defent ::gackground
  {:twenty18.render/square
   {:init {:position [0 0]
           :size [width height]
           :color "black"}}})

(defent ::npc
  {::clickable
   {:on-up
    (fn [this {:keys [x y]}]
      (-> this
        (update this :count inc)
        (assoc :position [x y])))}
   :twenty18.render/square
   {:init {:position [0 0]
           :size [30 30]
           :color "rgba(23, 230, 230, 0.8)"}}})

(defent ::pong
  {:twenty18.render/square
   {:init {:color "green" :position [50 50] :size [10 10]}}
   :twenty18.behavior/bounce
   {:params #{:speed}
    :init {:speed 5
           :x-factor 1
           :y-factor 1}
    :on-update
    (fn [this]
      this)}})

(defent ::pong
  {:twenty18.render/square
   {:init {:color "yellow" :position [50 30] :size [5 5]}}
   ::clickable {}})

(eve/start-game-loop!)

(let [parent (gdo/getElement "app")]
  (gdo/removeChildren parent)
  (gdo/appendChild parent canvas))

(defn on-js-reload [])
