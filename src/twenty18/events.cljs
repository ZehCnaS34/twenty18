(ns twenty18.events
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [twenty18.ecs :as ecs]
            [twenty18.utils :as utils]
            [goog.events :as gev]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(map ecs/deftrigger
  [::mouse-move
   ::mouse-down
   ::mouse-up
   ::mouse-enter
   ::mouse-out
   ::mouse-click
   ::update
   ::render
   ::init])

(def game-loop (atom nil))
(defonce *mouse-move* (atom nil))
(defonce *mouse-down* (atom nil))
(defonce *mouse-up* (atom nil))
(defonce *mouse-enter* (atom nil))
(defonce *mouse-out* (atom nil))

(defn handle-mouse-move [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (ecs/raise ::mouse-move {:position {:x x :y y}}))))

(defn handle-mouse-down [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (ecs/raise ::mouse-down {:event :mouse-down :position {:x x :y y}}))))

(defn handle-mouse-up [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (ecs/raise ::mouse-up {:event :mouse-up :position {:x x :y y}}))))

(defn handle-mouse-enter [c]
  (fn [event]
    (.preventDefault event)
    (ecs/raise ::mouse-enter {:event :enter})))

(defn handle-mouse-out [c]
  (fn [event]
    (.preventDefault event)
    (ecs/raise ::mouse-out {:event :out})))

(defn handle-mouse-click [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (ecs/raise ::mouse-up {:event :mouse-click :position {:x x :y y}}))))

(def event-map
  {"mousemove" handle-mouse-move
   "mousedown" handle-mouse-down
   "mouseup" handle-mouse-up
   "mouseenter" handle-mouse-enter
   "mouseout" handle-mouse-out
   "mouseclick" handle-mouse-click})

(defn attach-events [$elt]
  (doseq [[event f] event-map]
    (gev/listen $elt event (f nil))))

(defn start-game-loop! []
  (ecs/raise ::init)
  (swap! game-loop :with
    (go-loop []
      (ecs/raise ::update)
      (ecs/raise ::render)
      (utils/update-clock)
      (<! (timeout utils/fps))
      (recur))))

(defn stop-game-loop! [])
