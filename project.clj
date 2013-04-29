(defproject net.swiftkey/keysync-crate "0.8.0-SNAPSHOT"
  :description "Pallet Crate to install public keys to created nodes,
                from some shared blobstore."

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.palletops/pallet "0.8.0-beta.9"]
                 [org.jclouds/jclouds-blobstore "1.5.2"]])
