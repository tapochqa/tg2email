(ns tg2email.mailgun
  (:require
    [org.httpkit.client :as http]
    [cheshire.core :as json]
    
    [clojure.java.io :as io]))


(defn api-request
  [{:keys [domain mailgun-token]}
   method
   action
   multipart]
  
  @(http/request
    {:method method
     
     :url (format
            "https://api.mailgun.net/v3/%s/%s"
            domain
            (name action))
     
     :basic-auth 
     ["api" mailgun-token]
     
     :multipart 
     multipart}))


(defn send-email
  
  [{:keys [domain to] :as config} 
    subject
    multipart]
   
   (let [init-multipart
         [{:name "from"
           :content
           (format
            "💭 <mailgun@%s>"
            domain)}
          
          {:name "to"
           :content to}
          
          {:name "subject"
           :content (str subject)}]]
    
     (api-request 
          config
          :post
          :messages
          (concat
            init-multipart
            multipart))))





(comment
  
  
    (:body
      (send-email 
        {:mailgun-token 
         (slurp "mailgun-token")
                
         :domain 
         (slurp "domain")
         
         :to
         "choochooh55@gmail.com"}
        
        (System/currentTimeMillis)
        [{:name "attachment" 
          :content 
          (io/input-stream
            " ")}
         
         {:name "text" 
          :content " "}]))
    
  (io/input-stream 
      "https://bog.limonadny.ru/assets/photo_2021-11-23%2014.44.34.jpeg"))




