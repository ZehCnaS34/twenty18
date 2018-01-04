(ns twenty18.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [goog.dom :as gdo]
            [goog.events :as gev]
            [twenty18.v2 :as v2]
            [twenty18.ecs :as ecs :refer [defent]]
            [twenty18.events :as eve]
            [twenty18.utils :refer [fps now deltatime update-clock]]
            [twenty18.dom :refer [canvas]]
            [twenty18.render]
            [om.next :as om :refer-macros [defui]]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [om.dom :as dom]))

(enable-console-print!)

(def width (.-width canvas))
(def height (.-height canvas))

(defonce render-chan (chan))

(ecs/defcomp ::clickable
  {:twenty18.events/mouse-down
   (fn [this
        {:keys [position] :as payload}
        {:keys [on-down]}]
     (on-down this))
   :twenty18.events/mouse-up
   (fn [this {:keys [position]} {:keys [on-up]}]
     (on-up this))})

(defent ::gackground
  {:twenty18.render/square
   {:init {:position [0 0]
           :size [width height]
           :color "blue"}}})

(defent ::npc
  {::clickable
   {:on-down
     (fn [this]
       (update this :count inc))
    :on-up (fn [this] this)}
   :twenty18.render/square
   {:init {:position [0 0]}
    :on-click
     (fn [this top left]
       (-> this
         (assoc :position [left top])))
    :on-render
     (fn [this]
       (-> this
         (assoc :size [30 30])))}})



(eve/start-game-loop!)

(let [parent (gdo/getElement "app")]
  (gdo/removeChildren parent)
  (gdo/appendChild parent canvas))

(defn on-js-reload [])
