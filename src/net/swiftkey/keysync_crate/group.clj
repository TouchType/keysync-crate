(ns net.swiftkey.keysync-crate.group
  "Functions to manage keys store in the keysync blob.

   You can rebind *keysync-groups-blob* if you want to use a custom
   blob for storing user public keys."
  (:require [org.jclouds.blobstore2 :as blob]))

(def ^:dynamic *keysync-groups-blob* "keysync-groups")

(defn add-group!
  "Add a group to the blobstore."
  [blobstore group]
  nil)

(defn remove-group!
  "Remove a group from the blobstore."
  [blobstore group]
  nil)

(defn add-user!
  "Add a user's public key to the given group."
  [blobstore group user public-key]
  nil)

(defn remove-user!
  "Remove the given user's public key from this group."
  [blobstore group user]
  nil)

(defn list-groups
  "Return a set of the group names in this blobstore."
  [blobstore]
  #{})

(defn list-users
  "Return the set of user names in this group."
  [blobstore group]
  #{})

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
