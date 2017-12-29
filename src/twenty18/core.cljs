(ns twenty18.core
    (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
    (:require [goog.dom :as gdo]
              [goog.events :as gev]
              [twenty18.v2 :as v2]
              [twenty18.utils :refer [fps now deltatime update-clock]]
              [om.next :as om :refer-macros [defui]]
              [cljs.core.async :refer [put! chan <! >! timeout close!]]
              [om.dom :as dom]))

(enable-console-print!)

(defonce event-chan (chan))
(defonce env-chan (chan))
(defonce render-chan (chan))

(defn handle-mouse-move [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (go (>! event-chan {:event :move :pos {:x x :y y}}))
      )))

(defn handle-mouse-down [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (go (>! event-chan {:event :mouse-down :pos {:x x :y y}})))))

(defn handle-mouse-up [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (go (>! event-chan {:event :mouse-up :pos [:x x :y y]})))))

(defn handle-mouse-enter [c]
  (fn [event]
    (.preventDefault event)
    (go (>! c {:event :enter}))))

(defn handle-mouse-out [c]
  (fn [event]
    (.preventDefault event)
    (go (>! c {:event :out}))))

(def event-map
  {"mousemove" handle-mouse-move
   "mousedown" handle-mouse-down
   "mouseup" handle-mouse-up
   "mouseenter" handle-mouse-enter
   "mouseout" handle-mouse-out})

(def canvas
  (let [$c (gdo/createElement "canvas")]
    (doseq [[event f] event-map]
      (gev/listen $c event (f event-chan)))
    $c))

(def ctx (.getContext canvas "2d"))

(defn to->tos [f state] 
  (fn [[tag obj]]
    (f tag obj state)))

;; multis
(defmulti draw
  "draw item to context"
  (fn [ctx renderable] (:type renderable)))

(defmulti get-render
  (fn [[tag obj]] (:type obj)))

(defmulti obj
  (fn [tag obj state] (:type obj)))

;; STATE
(def app-state 
  (atom
    {:objects {:good-green {:type :pc
                            :pos {:x 40 :y 20}
                            :components [:walker]}
               :bad-blue {:type :npc
                          :pos {:x 30 :y 10}}
               :big-red {:type :npc
                         :pos {:x 0 :y 0}}}
     :offset [0 0]
     :pointer [0 0]}))

;;objects---------------------------------
(defmethod obj :default
  [tag {:keys [components] :as obj} state]
  {tag (reduce partial (map beh-fn components))})

;;drawers---------------------------------
(defmethod draw :default
  [ctx {:keys [type] :as obj}]
  (draw (assoc obj :type type)))

(defmethod draw :image
  [ctx {:keys [src]}]
  (let [im (js/Image.)]
    (set! (.-onload im) (fn [& args] 
                          (.drawImage ctx im 0 0)))
    (set! (.-src im) src)))

(defmethod draw :rect
  [ctx {:keys [left top width height color]}]
  (set! (.-fillStyle ctx) color)
  (.fillRect ctx left top width height))

(defmethod draw :text
  [ctx {:keys [text font top left]}]
  (set! (.-font ctx) font)
  (.fillText ctx text left top))

;;renderers-------------------------------
(defmethod get-render :default
  [[tag {:keys [type]}]]
  {:type :text
   :font "15px Arial"
   :text " renderer not implmented for "
   :top 10 
   :left 30})

(defmethod get-render :health
  [[tag {:keys [pos]}]]
  {:type :image
   :src "/images/heart.png"
   :top (:y pos)
   :left (:x pos)
   })

(defmethod get-render :pc
  [[tag {:keys [pos]}]]
  {:type :rect
   :top (:y pos)
   :left (:x pos)
   :color "rgb(0,200,0)"
   :height 10
   :width 10})

(defmethod get-render :npc
  [[tag {:keys [pos]}]]
  {:type :rect
   :top (:y pos)
   :left (:x pos)
   :color "rgb(200,0,0)"
   :height 10
   :width 10})

(def environment
  (go-loop []
           (let [obj-fn (to->tos obj @app-state)
                 objects (apply merge (map obj-fn (:objects @app-state)))]
             (swap! app-state assoc-in [:objects] objects)
             (>! env-chan objects)
             (<! (timeout fps))
             (recur))))

;;; GAME-LOOP
; update player positions
; get-renderables for each player
(def game-loop 
  (go-loop 
    []
    (alt!
      event-chan ([{:keys [event] :as payload}] 
                  (cond
                    :default nil
                    ))
      env-chan ([objs] (>! render-chan (map get-render objs)))
      )
    (<! (timeout 100))
    (recur)))

(def render-loop
  (go-loop []
    (let [renderables (<! render-chan)]
      (doseq [renderable renderables]
        (draw ctx renderable))
      (update-clock)
      (<! (timeout fps))
      (recur))))

(let [parent (gdo/getElement "app")]
  (gdo/removeChildren parent)
  (gdo/appendChild parent canvas)
  )

(defn on-js-reload [])
