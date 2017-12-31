(ns twenty18.events
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [twenty18.ecs :as ecs]
            [goog.events :as gev]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]))

(ecs/deftrigger ::mouse-mouse)

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
      (swap! *mouse-move* :with {:pos {:x x :y y}})
      (ecs/raise ::mouse-move {:pos {:x x :y y}}))))

(defn handle-mouse-down [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (swap! *mouse-down* :with {:event :mouse-down :pos {:x x :y y}}))))

(defn handle-mouse-up [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (swap! *mouse-up* :with {:event :mouse-up :pos [:x x :y y]}))))

(defn handle-mouse-enter [c]
  (fn [event]
    (.preventDefault event)
    (swap! *mouse-enter* :with {:event :enter})))

(defn handle-mouse-out [c]
  (fn [event]
    (.preventDefault event)
    (swap! *mouse-out* :with {:event :out})))

(def event-map
  {"mousemove" handle-mouse-move
   "mousedown" handle-mouse-down
   "mouseup" handle-mouse-up
   "mouseenter" handle-mouse-enter
   "mouseout" handle-mouse-out})

(defn attach-events [$elt]
  (doseq [[event f] event-map]
    (gev/listen $elt event (f nil))))

(defn mouse-move-event [] @*mouse-move*)
