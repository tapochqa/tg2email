(ns tg2email.processing
  (:require
    [tg-bot-api.telegram :as telegram]
    [clojure.java.io :as io]))

(defn text-or-caption 
    [{text :text :as message}]
    (str (or text
                    (last (find message :caption)))))


(defn map-message 
    ([name username message]
       {:name (str name)
        :username (str username)
        :text (text-or-caption message)})
    ([first-name last-name username message]
       {:name (str first-name (if last-name (str " " last-name) nil))
        :username (str username)
        :text (text-or-caption message)}))


(defn if-channel 
    [{{ title :title 
        username :username 
        :as forward} :forward_from_chat :as message}]
    (if (some? forward)
        (map-message 
            title
            username
            message)))


(defn if-open-dm 
    [{{ first-name :first_name 
        last-name :last_name 
        username :username 
        :as forward} :forward_from :as message}]
    (if (some? forward) 
           (map-message
                first-name
                last-name
                username
                message)))


(defn if-closed-dm 
    [{  name :forward_sender_name  
        :as message}] 
    (if (some? name)
            (map-message
                name
                (str "")
                message)))


(defn if-straight 
    [{{ first-name :first_name
        last-name :last_name
        username :username 
        :as from} :from :as message}]
    (map-message
        first-name
        last-name
        username
        message))



(defn find-body-key [message]
    (map 
        (fn [a b]  (find a b)) 
        (repeat 8 message) 
        '(:photo :text :contact :sticker :video :video_note :voice :audio)))


(defn untextize [string]
    (subs (clojure.string/replace string #"text" "") 1))


(defn media-type [message]
    (->> message
        find-body-key
        (remove nil?)
        flatten
        first
        str
        untextize))


(defn seek [message]
    (conj {:media_type (media-type message)}
            (or (if-channel message)
            (if-open-dm message)
            (if-closed-dm message)
            (if-straight message))))
    

(defn compile-theme [message]
    (let [{name :name username :username text :text media :media_type} (seek message)]
        (format "%s (@%s): %s %s" 
                name 
                username
                (str media)
                text)))


(defn tg-link 
    [config message {id :file_id size :file_size}]
    (if (< size 20000000)
        

        [{  :name "attachment"
            :content (io/input-stream
                       (str "https://api.telegram.org/file/bot"
                                                (:token config)
                                                "/"
                                                (:file_path 
                                                  (telegram/get-file config id))))}
        {   :name "text"
            :content (str (text-or-caption message) " ")}]

        [{  :name "text"
            :content (str (text-or-caption message) " 🚫✉️ файл больше 20-ти мегабайт.")}]))


(defn media->link [config message coll]
    (if (= (type coll) java.lang.String)
        [{   :name "text"
             :content coll}]
        (tg-link config message coll)))


(defn process-body [config message]
    (->> message
            find-body-key
            (remove nil?)
            flatten
            (drop 1)
            (sort-by :file_size)
            last
            (media->link config message)))