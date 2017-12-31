(ns twenty18.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [goog.dom :as gdo]
            [goog.events :as gev]
            [twenty18.v2 :as v2]
            [twenty18.ecs :as ecs :refer [defent]]
            [twenty18.events :as eve]
            [twenty18.utils :refer [fps now deltatime update-clock]]
            [om.next :as om :refer-macros [defui]]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [om.dom :as dom]))

(enable-console-print!)

(defonce render-chan (chan))

(def canvas (gdo/createElement "canvas"))
(eve/attach-events canvas)
(def ctx (.getContext canvas "2d"))

(def game-loop)
(def render-loop)

(let [parent (gdo/getElement "app")]
  (gdo/removeChildren parent)
  (gdo/appendChild parent canvas))

(defn on-js-reload [])
