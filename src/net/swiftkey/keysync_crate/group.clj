(ns net.swiftkey.keysync-crate.group
  "Functions to manage keys store in the keysync blob.

   You can rebind *keysync-groups-container* if you want to use a custom
   bucket for storing user public keys."
  (:require [clojure.edn :as edn]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [org.jclouds.blobstore2 :as blob])
  (:import  [java.io PushbackReader]))

(def ^:dynamic *keysync-groups-container* "keysync-groups")

(defmacro ^:private with-keysync-container
  [blobstore & body]
  `(do
     (when-not (blob/container-exists? ~blobstore *keysync-groups-container*)
       (blob/create-container ~blobstore *keysync-groups-container*
                              :public-read? false))
     ~@body))

(defmacro ^:private with-check-group-not-exists
  [blobstore group & body]
  `(if (blob/blob-exists? ~blobstore *keysync-groups-container* ~group)
     (throw (Exception. (str "Attempt to overwrite existing group: " ~group)))
     (do ~@body)))

(defmacro ^:private with-check-group-exists
  [blobstore group & body]
  `(if-not (blob/blob-exists? ~blobstore *keysync-groups-container* ~group)
     (throw (Exception. (str "Attempt to use missing group: " ~group)))
     (do ~@body)))

(defn- edn->blob
  "Create a blob for some Clojure data."
  [name val]
  (blob/blob name
             :payload (prn-str val)
             :content-type "application/edn"))

(defn- read-edn
  "Restore Clojure data from blobstore."
  [blobstore path]
  (when (blob/blob-exists? blobstore *keysync-groups-container* path)
    (edn/read
     (PushbackReader.
      (io/reader
       (blob/get-blob-stream blobstore *keysync-groups-container* path))))))

(defn- swap-edn!
  "A swap-like function for values in the blobstore."
  [blobstore key update-fn]
  (->> (read-edn blobstore key)
       (update-fn)
       (edn->blob key)
       (blob/put-blob blobstore *keysync-groups-container*)))

(defn add-group!
  "Add a group to the blobstore."
  [blobstore group]
  (with-keysync-container blobstore
    (with-check-group-not-exists blobstore group
      (swap-edn! blobstore group (constantly {})))))

(defn remove-group!
  "Remove a group from the blobstore."
  [blobstore group]
  (with-keysync-container blobstore
    (with-check-group-exists blobstore group
      ;; For reasons I don't understand, this doesn't actually delete the
      ;; key, but replaces it with an empty value.
      (blob/remove-blob      blobstore *keysync-groups-container* group)
      (blob/delete-directory blobstore *keysync-groups-container* group))))

(defn add-user!
  "Add a user's public key to the given group."
  [blobstore group user public-key]
  (with-keysync-container blobstore
    (with-check-group-exists blobstore group
      (swap-edn! blobstore group (fn [g] (assoc g user public-key))))))

(defn remove-user!
  "Remove the given user's public key from this group."
  [blobstore group user]
  (with-keysync-container blobstore
    (with-check-group-exists blobstore group
      (swap-edn! blobstore group (fn [g] (dissoc g user))))))

(defn list-groups
  "Return a set of the group names in this blobstore."
  [blobstore]
  (with-keysync-container blobstore
    (into #{} (map blob/blob-name
                   (blob/blobs blobstore *keysync-groups-container*)))))

(defn list-users
  "Return the set of user names in this group."
  [blobstore group]
  (with-keysync-container blobstore
    (with-check-group-exists blobstore group
      (into #{} (keys (read-edn blobstore group))))))

(defn authorized-keys
  "Return a string containing the content for an OpenSSH
   authorized_keys file, authorizing users of the requested
   groups."
  [blobstore & groups]
  "")

(defn revoke-all!
  "Revoke access to all groups for these users.

   NOTE - This just updates the state of the blobstore, so you'll
          need to run the appropriate phase on any deployed nodes
          for this to take effect!"
  [blobstore & users]
  nil)
