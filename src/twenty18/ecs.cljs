(ns twenty18.ecs
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [twenty18.utils :refer [fps]]))

(def *entities* (atom {}))
(def *components* (atom {}))
(def *tags* (atom {}))
(def *triggers* (atom #{}))

(def *raises* (chan))

(defn defent
  "Register an entity in the CES (BOT) system"
  [name tags components]
  (swap! *entities* merge {name {::name name
                                 ::tags tags
                                 ::components components}}))

(defn defcomp
  "Register a component"
  ([name handler-map]
   (swap! *components* merge {name (-> handler-map
                                     (assoc ::name name))}))
  ([name triggers handler-map]
   (swap! *components* merge {name (-> handler-map
                                     (assoc ::triggers triggers)
                                     (assoc ::name name))})))

(defn deftrigger [name]
  (swap! *triggers* conj name))

(defn raise [trigger-name & payload]
  (go (>! *raises* trigger-name)))

(def raise-loop
  (go-loop []
    (let []
      (<! (timeout 200))
      (recur))))

;; EXAMPLES
(comment
  (defent ::example
    [:npc]
    [::walker])

  (defcomp ::walker
    {:update
      (fn [ent]
        (println "This gets called when update is called")
        ent)}))
