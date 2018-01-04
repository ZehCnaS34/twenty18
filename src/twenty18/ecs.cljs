(ns twenty18.ecs
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [twenty18.utils :refer [fps]]))

(def *entities* (atom {}))
(def *components* (atom {}))
(def *tags* (atom {}))
(def *triggers* (atom #{}))
(def eid (atom 0))
(defn id [] (swap! eid inc))

(def *raises* (chan))

(defn defent
  "Register an entity in the CES (BOT) system"
  ([name components]
   (defent name #{} components))
  ([name tags components]
   (swap! *entities* merge {name {::name name
                                  ::tags tags
                                  ::id (id)
                                  ::components components}})))

(defn ent->comps [ent] (::components ent))

(defn entity->components [e]
  (let [c-names (keys (::components e))
        components @*components*]
    (select-keys components c-names)))

(defn component->handlers [c handler]
  (let [h-names (keys (::handlers c))]))

(defn defcomp
  "Register a component"
  ([name handler-map]
   (swap! *components* merge
     {name (-> {}
             (assoc ::name name)
             (assoc ::handlers handler-map))})))

(defn deftrigger [name]
  (swap! *triggers* conj name))

(defn raise
  ([trigger-name]
   (raise trigger-name {}))
  ([trigger-name payload]
   (go (>! *raises* {::name trigger-name ::payload payload}))))

(def raise-loop
  (go-loop []
    (let [item (<! *raises*)
          trigger (::name item)
          payload (::payload item)
          entities @*entities*]
      (doseq [[e-name entity] entities]
        (doseq [[c-name component] (entity->components entity)
                :when (and (contains? (::components entity) c-name)
                           (-> component ::handlers trigger))
                :let [f (-> component ::handlers trigger)
                      handlers (-> entity ::components c-name)
                      updated-entity (f entity payload handlers)]]
          (when (= (::id entity) (::id updated-entity))
            (swap! *entities* assoc e-name updated-entity))))
      (recur))))

;; EXAMPLES
(comment
  (defent ::example
    #{:npc :enemy}
    {:twenty18.v2/collider
     {:params {:width 1 :height 1}
      :handlers
      {:on-hit
       (fn [this other]
         (println this other))}}
     :twenty18.v2/transform {}})

  (defcomp ::collider
    {:on-update
     (fn [this env width height]
       (let [pos (:position this)])
       this)}))
