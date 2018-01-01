(ns twenty18.dom
  (:require [goog.dom :as gdo]
            [twenty18.events :as eve]))

(def canvas (gdo/createElement "canvas"))
(eve/attach-events canvas)
(def ctx (.getContext canvas "2d"))