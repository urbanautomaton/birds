(ns birds.core
  (:require [clojure.browser.repl :as repl]
            [reagent.core :as r]))

;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(println "Hello world!")

(def radians-per-second (r/atom 0.4))
(def particles (r/atom [[-1 -1] [-1 1] [1 1] [1 -1]]))
(def last-ts (r/atom nil))

(defn rotation-by [theta]
  [[(Math/cos theta) (- (Math/sin theta))]
   [(Math/sin theta) (Math/cos theta)]])

(defn radians-to-rotate [delta-t]
  (/ (* delta-t @radians-per-second)
     1000))

(defn rotate [theta [x y]]
  (let [[[a b] [c d]] (rotation-by theta)]
    [(+ (* a x) (* b y))
     (+ (* c x) (* d y))]))

(defn point-to-circle [[x y]]
  [:circle {:cx x :cy y :r 0.1}])

(defn move-dots! [delta-t]
  (swap! particles
         #(map (partial rotate (radians-to-rotate delta-t))
               %1)))

(defn dots []
  (into [:svg {:width 300 :height 300 :viewBox "-2 -2 4 4"}]
        (map point-to-circle @particles)))

(defn update! [ts]
  (let [delta-t (- ts (or @last-ts ts))]
    (move-dots! delta-t)
    (r/render-component [:div [:div delta-t] (dots)]
                        (js/document.getElementById "app")))
  (reset! last-ts ts)
  (. js/window (requestAnimationFrame update!)))

(defn ^:export run []
  (. js/window (requestAnimationFrame update!)))
