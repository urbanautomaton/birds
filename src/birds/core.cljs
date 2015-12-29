(ns birds.core
  (:require [clojure.browser.repl :as repl]
            [reagent.core :as r]
            [goog.string :as gstring]
            [goog.string.format]))

;; (defonce conn
;;   (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(println "Hello world!")

(defn random-point-in-unit-circle []
  (let [r (rand 2)
        theta (* (rand) 2 Math/PI)]
    [(* r (Math/cos theta))
     (* r (Math/sin theta))]))

(def number-of-points 50)

(def animation-config (r/atom {:radians-per-second 0.4 :animating true}))
(def particles (r/atom (map random-point-in-unit-circle (range 1 number-of-points))))
(def last-ts (r/atom nil))
(def fps (r/atom 0.0))

(defn get-config [param]
  (@animation-config param))

(defn set-config! [param value]
  (println "Setting " param " to " value)
  (swap! animation-config assoc param value))

(defn toggle! [param]
  (let [newval (not (@animation-config param))]
    (set-config! param newval)))

(defn rotation-by [theta]
  [[(Math/cos theta) (- (Math/sin theta))]
   [(Math/sin theta) (Math/cos theta)]])

(defn radians-to-rotate [delta-t]
  (/ (* delta-t (get-config :radians-per-second))
     1000))

(defn rotate [theta [x y]]
  (let [[[a b] [c d]] (rotation-by theta)]
    [(+ (* a x) (* b y))
     (+ (* c x) (* d y))]))

(defn point-to-circle [[x y]]
  [:circle {:cx x :cy y :r 0.05}])

(defn move-dots! [delta-t]
  (swap! particles
         #(map (partial rotate (radians-to-rotate delta-t))
               %1)))

(defn checkbox [param]
  [:div {:class "checkbox"}
   [:label
    [:input {:type "checkbox" :checked (get-config param)
             :on-change #(toggle! param)}]
    (name param)]])

(defn slider [param min max]
  [:div {:class "slider"}
   [:label (name param)
    [:span {:class "slider__value"} (get-config :radians-per-second)]
    [:br]
    [:input {:type "range" :value (get-config param) :min min :max max :step 0.01
             :on-change (fn [e] (set-config! param (.-target.value e)))}]]])

(defn fps-display [value]
  [:span (gstring/format "%0.2f fps" value)])

(defn birds-svg []
  [:div {:id "animation"}
   (into [:svg {:width 300 :height 300 :viewBox "-2 -2 4 4"}]
         (map point-to-circle @particles))])

(defn update! [ts]
  (let [delta-t (- ts (or @last-ts ts))]
    (when (get-config :animating)
      (move-dots! delta-t))
    (reset! last-ts ts)
    (reset! fps (/ 1 (/ delta-t 1000)))
    (. js/window (requestAnimationFrame update!))))

(defn bird-component []
  [:div
   [slider :radians-per-second 0.1 5.0]
   [checkbox :animating]
   [fps-display @fps]
   [birds-svg]])

(defn bird-app []
  (. js/window (requestAnimationFrame update!))
  [bird-component])

(defn ^:export run []
  (r/render [bird-app]
            (js/document.getElementById "app")))
