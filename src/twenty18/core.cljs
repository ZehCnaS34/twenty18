(ns twenty18.core
    (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
    (:require [goog.dom :as gdo]
              [goog.events :as gev]
              [om.next :as om :refer-macros [defui]]
              [cljs.core.async :refer [put! chan <! >! timeout close!]]
              [om.dom :as dom]))

(enable-console-print!)

;; 60 fps please ~~ 18 milliseconds
(def fps (int (/ 1000 60)))

;; TIMING AND CLOCK STUFF
(defn now [] (.getTime (js/Date.)))

(def clock
  (atom (now)))

(defn deltatime []
  (- (now) @clock))

;; STATE
(defonce event-chan (chan))
(defonce env-chan (chan))
(defonce render-chan (chan))

(def components
  {})

(def players
  {})

(def app-state 
  (atom
    {:behaviours #{{:type :goto
                    :target :health
                    :player :big-red}}
     :actions #queue[]
     :objects [{:type :health
                :tag :health}]
     ; :players [{:type :pc
     ;            :tag :good-green
     ;            :pos {:x 40 :y 20}}
     ;           {:type :npc
     ;            :tag :big-red
     ;            :pos {:x 0 :y 0}}]
     :players {:good-green {:type :pc
                            :pos {:x 40 :y 20}}
               :big-red {:type :npc
                         :pos {:x 0 :y 0}}}
     :player :good-green
     :offset [0 0]
     :pointer [0 0]}))

(defn handle-mouse-move [c]
  (fn [event]
    (.preventDefault event)
    (let [x (.-offsetX event)
          y (.-offsetY event)]
      (swap! app-state assoc-in [:pointer] {:x x :y y})
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

(defmulti act (fn [action state] (:type action)))
(defmethod act :default
  [action state]
  state)
(defmethod act :select
  [action state])

; act method takes the behaviour objects and a state and returns the mutated state
(defmulti beh (fn [obj state] (:type obj)))
(defmethod beh :default
  [obj state]
  state)
(defmethod beh :goto
  [{:keys [target player]} state])

(defmulti npc (fn [obj] (:type obj)))
(defmethod npc :default
  [obj]
  obj)

(defmulti draw (fn [ctx obj] (:type obj)))
(defmethod draw :default
  [ctx {:keys [type] :as obj}]
  (draw (assoc obj :type type)))

(defmethod draw :rect
  [ctx {:keys [left top width height color]}]
  (set! (.-fillStyle ctx) color)
  (.fillRect ctx left top width height))

(defmulti get-render (fn [[tag obj]] (:type obj)))
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
  (let [players (map npc (:players @app-state))]
    (swap! app-state assoc-in [:players] players)
    (go-loop []
             (>! env-chan players)
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
                    (= event :mouse-down) 
                    (let [{:keys [pos]} payload]
                      (swap! app-state update :actions conj {:type :select :pos pos}))
                    
                    :default nil
                    ))
      env-chan ([objs] (>! render-chan (map get-render objs)))
      )
    (recur)))

(def render-loop
  (go-loop []
    (let [renderables (<! render-chan)]
      (doseq [renderable renderables]
        (draw ctx renderable))
      (swap! clock :with (now))
      (<! (timeout fps))
      (recur))))

; (defn stop-loops [& args]
;   (js/clearInterval environment-loop)
;   (js/clearInterval render-loop)
;   )

; (defn start-loops [& args]
;   (js/clearInterval environment-loop)
;   (js/clearInterval render-loop)
;   )

; (def stop-button
;   (let [btn (gdo/createElement "button")]
;     (gev/listen btn "click" stop-loops)
;     (gdo/appendChild btn (gdo/createTextNode "stop"))
;     btn))

; (def start-button
;   (let [btn (gdo/createElement "button")]
;     (gev/listen btn "click" start-loops)
;     (gdo/appendChild btn (gdo/createTextNode "start"))
;     btn))

(let [parent (gdo/getElement "app")]
  (gdo/removeChildren parent)
  (gdo/appendChild parent canvas)
  ; (gdo/appendChild parent stop-button)
  ; (gdo/appendChild parent start-button)
  )



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
