(ns net.swiftkey.keysync-crate.group-test
  (:require [clojure.test :refer :all]
            [net.swiftkey.keysync-crate.group :as sut]
            [org.jclouds.blobstore2 :as blob]))

(defn transient-blobstore [] (blob/blobstore "transient" "" ""))

(deftest test-group
  (let [bs (transient-blobstore)]
    (sut/add-group! bs "test-container" "test-group-1")
    (is (= (sut/list-groups bs "test-container") #{"test-group-1"}))

    (sut/add-group! bs "test-container" "test-group-2")
    (is (= (sut/list-groups bs "test-container") #{"test-group-1" "test-group-2"}))

    (sut/remove-group! bs "test-container" "test-group-1")
    (is (= (sut/list-groups bs "test-container") #{"test-group-2"}))))

(deftest test-add-user
  (let [bs (transient-blobstore)]
    (sut/add-group! bs "test-container" "test-group-1")
    (sut/add-group! bs "test-container" "test-group-2")

    (sut/add-user! bs "test-container" "test-group-1" "user-1" "public-key-1")
    (is (= (sut/list-users bs "test-container" "test-group-1") #{"user-1"}))
    (is (empty? (sut/list-users bs "test-container" "test-group-2")))

    (sut/add-user! bs "test-container" "test-group-2" "user-2" "public-key-2")
    (is (= (sut/list-users bs "test-container" "test-group-1") #{"user-1"}))
    (is (= (sut/list-users bs "test-container" "test-group-2") #{"user-2"}))

    (sut/remove-user! bs "test-container" "test-group-1" "user-1")
    (is (empty? (sut/list-users bs "test-container" "test-group-1")))
    (is (= (sut/list-users bs "test-container" "test-group-2") #{"user-2"}))))

(deftest test-authroized-keys
  (let [bs (transient-blobstore)]
    (sut/add-group! bs "test-container" "test-group-1")
    (sut/add-group! bs "test-container" "test-group-2")
    (sut/add-group! bs "test-container" "test-group-3")

    (sut/add-user! bs "test-container" "test-group-1" "user-1" "key1")
    (sut/add-user! bs "test-container" "test-group-1" "user-2" "key2\n")
    (sut/add-user! bs "test-container" "test-group-2" "user-1" "key1")
    (sut/add-user! bs "test-container" "test-group-2" "user-3" "key3")
    (sut/add-user! bs "test-container" "test-group-3" "user-4" "key4")

    (let [auth-1 (sut/authorized-keys bs "test-container" "test-group-1")]
      (is (re-find #"key1" auth-1))
      (is (re-find #"key2" auth-1))
      (is (not (re-find #"key[34]" auth-1))))

    (let [auth-1-2 (sut/authorized-keys bs "test-container" "test-group-1" "test-group-2")
          keys-1-2 (.split auth-1-2 "\n")]
      (is (not (re-find #"key4" auth-1-2)))
      (is (= (count keys-1-2) 3))
      (is (= (set keys-1-2) #{"key1" "key2" "key3"})))))

(deftest test-revoke-all
  (let [bs (transient-blobstore)]
    (sut/add-group! bs "test-container" "test-group-1")
    (sut/add-group! bs "test-container" "test-group-2")
    (sut/add-user!  bs "test-container" "test-group-1" "user-1" "key")
    (sut/add-user!  bs "test-container" "test-group-1" "user-2" "key")
    (sut/add-user!  bs "test-container" "test-group-2" "user-1" "key")
    (sut/add-user!  bs "test-container" "test-group-2" "user-3" "key")

    (sut/revoke-all! bs "test-container" "user-1" "user-3")
    (is (= (sut/list-users bs "test-container" "test-group-1") #{"user-2"}))
    (is (empty? (sut/list-users bs "test-container" "test-group-2")))))
