(ns net.swiftkey.keysync-crate.core
  "Pallet Plan for synchronizing public keys from a shared blobstore.

   Use functions in net.swiftkey.keysync-crate.group to create groups
   and add users and keys to them. You can then use the authorize-groups
   plan here to synchronize the appropriate public keys to the admin
   user on created nodes."
  (:require [pallet.crate :refer [defplan] :as crate]
            [pallet.actions :refer [remote-file]]
            [pallet.core.session :refer [session]]
            [net.swiftkey.keysync-crate.group :as group])
  (:import  [java.io File]))

(defn user-auth-file-path
  "Get the destination auth file path for the given user."
  [user]
  (.getAbsolutePath
   (File. (.getParent (File. (:public-key-path user)))
          "authorized_keys")))

(defplan authorize-groups
  "Authorize the given groups to be authenticated by their public
   keys for the given user account (defaults to current admin user).
   `container` should be a string - the container name to look up groups in."
  [container groups & {:keys [user blobstore]
                       :or   {user (crate/admin-user)
                              blobstore (-> (session) :environment :blobstore)}}]
  {:pre [(coll? groups)]}
  (remote-file (user-auth-file-path user)
               :content (apply group/authorized-keys blobstore container groups)
               :force true
               :mode "644"
               :owner (:username user)))
