# keysync

This is a simple crate for Pallet to help share admin access to servers
controlled by Pallet among many team members. It works by storing groups
of SSH public keys in some kind of jclouds Blobstore (i.e. S3) and provides
a single Plan function to synchonrize the keys onto the node in an
`authorized-keys` file for the admin account.

## Leiningen

The crate is in Clojars, so:

    [net.swiftkey/keysync-crate "0.8.0-SNAPSHOT"]

We'll try to make sure the version numbers align well enough with Pallet that
it should be obvious which version of Pallet it's compatible with.

## Adding Users/Groups

The `net.swiftkey.keysync-crate.group` namespace provides a simple API for
managing user groups in the Blobstore. The idea is that you either use the REPL
to do some small one-off admin tasks, or write a small app if you need to do
some bulk operations. Here's a quick walkthrough:

```clojure
;; Use the jclouds Blobstore API directly to connect to your store
(require '[org.jclouds.blobstore2 :as blob])
(def bs (blob/blobstore "aws-s3" "access-key" "secret-key"))

;; Now you can use the functions in the group namespace to manage
;; your user groups
(require '[net.swiftkey.keysync-crate.group :as group])

;; Create a group
(group/add-group! bs "admins")
(group/add-group! bs "special")

;; Add some users
(group/add-user! bs "admins" "jeremy" "<ssh public key as string>")
(group/add-user! bs "admins" "charles" "<ssh public key as string>")
(group/add-user! bs "special-guys" "bill" "<ssh public key as string>")
```

You get the idea! There are also functions to remove groups, remove
users and revoke all access for a particular user. See the doc strings
in the `group` namespace.

## Synchronizing Keys

When you have a Blobstore container set up with the groups and users you
need, you can use the single plan function in `net.swiftkey.keysync-crate.core`
to install a selection of the keys to the node.

```clojure
(require '[net.swiftkey.keysync-crate.core :as keysync])

(def my-group-spec
  (group-spec "my-group-spec"
    :phases {:bootstrap (plan-fn (automated-admin-user))
             :keysync   (plan-fn
                          (keysync/authorize-groups "admins" "special"))}))
```

Running the `:keysync` phase will overwrite the `authorized_keys` for the admin
account with the current state of the blobstore. You can provide optional params
to override the blobstore (source) and the user account.

## Things to Note

This approach is suboptimal in many ways. We'd love to see something better, but
meanwhile, it does the job. Keep the following subtleties/limitations in mind
when you're using the keysync crate:

* If you have a guy "go rogue" or leave your organization, you'll want to revoke
  access to any deployed servers with his/her public key authorized. The
  `revoke-all!` function in `group` makes the changes required to the blobstore,
  but you still need an authorized user to re-run the appropriate phase on all
  deployed nodes before access is revoked.
* You can easily shoot yourself in the foot. If you sync keys onto a server that
  doesn't include the key being used by Pallet, you'll lock yourself out. So,
  don't do that :-)
* Remember that the default name for the admin user will be your local username.
  When you're sharing access with other team members, you'll probably want to
  change the username to save confusion.

## License

Copyright (C) 2013 TouchType Ltd.

Distributed under the Eclipse Public License, the same as Clojure.
