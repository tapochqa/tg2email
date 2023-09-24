(ns tg2email.handling
  (:require
    [tg-bot-api.telegram :as telegram]
    [tg2email.processing :as processing]
    [tg2email.mailgun :as mailgun]))


(defn the-handler 
  "Bot logic here"
  [config {:keys [message]} trigger-id]
  
  (mailgun/send-email
    config
    (processing/compile-theme message)
    (processing/process-body config message))
  (telegram/send-message config 
    (-> message :chat :id)
    "ок"))
