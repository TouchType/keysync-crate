(defproject net.swiftkey/keysync-crate "0.8.0-SNAPSHOT"
  :description "Pallet Crate to install public keys to created nodes,
                from some shared blobstore."

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[com.palletops/pallet-lein "0.6.0-beta.9"]]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.palletops/pallet "0.8.0-beta.9"]
                 [org.jclouds/jclouds-blobstore "1.5.5"]]

  :profiles {:pallet
             {:dependencies [[org.cloudhoist/pallet-jclouds "1.5.2"]
                             [org.jclouds.provider/aws-ec2 "1.5.5"]
                             [org.jclouds.provider/aws-s3 "1.5.5"]
                             [org.jclouds.driver/jclouds-slf4j "1.5.5"]
                             [org.jclouds.driver/jclouds-sshj "1.5.5"]]
              :repositories {"sonatype" "http://oss.sonatype.org/content/repositories/releases"}}})
