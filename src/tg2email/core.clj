(ns tg2email.core 
  (:gen-class)
  (:require
    [tg2email.polling  :as polling]
    [tg2email.lambda   :as lambda]
    [clojure.string    :as str]
    [cheshire.core     :as json]))


(defn polling
  [config]
  (polling/run-polling config))

(defn lambda
  [config]
  (-> (lambda/->request config)
      (lambda/handle-request! config)
      (lambda/response->)))

(defn -main
  [tg-token
   mailgun-token
   domain
   to]
  
  (let [config 
        { :test-server false
          :token tg-token
          :mailgun-token mailgun-token
          :domain domain
          :to to
          :polling {:update-timeout 1000}
          }]
  #_(polling/run-polling config)
  (lambda config)))


(comment
  
   (binding [*in* (-> "trigger-request.json"
                 clojure.java.io/resource
                 clojure.java.io/reader)]
     
     (-main "...:..."))
  
  (-main
    (slurp "tg-token")
    (slurp "mailgun-token")
    (slurp "domain")
    "choochooh55@gmail.com"
    )
  (-main "...:...")
  
  )
