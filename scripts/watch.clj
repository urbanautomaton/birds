(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'birds.core
   :output-to "out/birds.js"
   :output-dir "out"})
