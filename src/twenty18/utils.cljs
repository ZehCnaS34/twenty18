(ns twenty18.utils)

(def fps (int (/ 1000 60)))

(defn now [] (.getTime (js/Date.)))

(def clock
  (atom (now)))

(defn deltatime []
  (- (now) @clock))

(defn update-clock []
  (swap! clock :with (now)))
