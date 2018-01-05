(ns twenty18.dom
  (:require [goog.dom :as gdo]
            [twenty18.v2 :as v2]
            [twenty18.events :as eve]))

(def canvas (gdo/createElement "canvas"))
(eve/attach-events canvas)

(def ctx (.getContext canvas "2d"))
(set! (.-width (.-canvas ctx)) (.-innerWidth js/window))
(set! (.-height (.-canvas ctx)) (.-innerHeight js/window))

(defn view-size []
  (v2/box {:x 0
           :y 0
           :height (.-height canvas)
           :width (.-width canvas)}))
