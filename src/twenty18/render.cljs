(ns twenty18.render
  (:require [twenty18.ecs :refer [defcomp deftrigger raise]]
            [twenty18.dom :refer [ctx]]))


(defcomp ::renderable
  {:twenty18.events/render
   (fn [ent _ {:keys [on-render]}]
     (set! (.-fillStyle ctx) "rgb(200, 0, 0)")
     (.fillRect ctx 0 0 10 10)
     (on-render ent))})

; (defmethod draw :default
;   [ctx {:keys [type] :as obj}]
;   (draw (assoc obj :type type)))
;
; (defmethod draw :image
;   [ctx {:keys [src]}]
;   (let [im (js/Image.)]
;     (set! (.-onload im) (fn [& args]
;                           (.drawImage ctx im 0 0)))
;     (set! (.-src im) src)))
;
; (defmethod draw :rect
;   [ctx {:keys [left top width height color]}]
;   (set! (.-fillStyle ctx) color)
;   (.fillRect ctx left top width height))
;
; (defmethod draw :text
;   [ctx {:keys [text font top left]}]
;   (set! (.-font ctx) font)
;   (.fillText ctx text left top))
;
; ;;renderers-------------------------------
; (defmethod get-render :default
;   [[tag {:keys [type]}]]
;   {:type :text
;    :font "15px Arial"
;    :text " renderer not implmented for "
;    :top 10
;    :left 30})
;
; (defmethod get-render :health
;   [[tag {:keys [pos]}]]
;   {:type :image
;    :src "/images/heart.png"
;    :top (:y pos)
;    :left (:x pos)})
;
;
; (defmethod get-render :pc
;   [[tag {:keys [pos]}]]
;   {:type :rect
;    :top (:y pos)
;    :left (:x pos)
;    :color "rgb(0,200,0)"
;    :height 10
;    :width 10})
;
; (defmethod get-render :npc
;   [[tag {:keys [pos]}]]
;   {:type :rect
;    :top (:y pos)
;    :left (:x pos)
;    :color "rgb(200,0,0)"
;    :height 10
;    :width 10})
