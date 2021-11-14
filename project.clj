(def matrix-profiles
  '{:matrix-clj-1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
    :matrix-clj-1.9 {:dependencies [[org.clojure/clojure "1.9.0"]]}
    :matrix-clj-1.10 {:dependencies [[org.clojure/clojure "1.10.3"]]}
    :matrix-clj-1.11 {:dependencies [[org.clojure/clojure "1.11.0-master-SNAPSHOT"]]
                      :repositories [["sonatype-oss-public" {:url "https://oss.sonatype.org/content/groups/public"}]]}
    :matrix-cljs-1.10.879 {:dependencies [[org.clojure/clojurescript "1.10.879"]]}
    :matrix-cljs-1.10.891 {:dependencies [[org.clojure/clojurescript "1.10.891"]]}})

(require '[clojure.string :as str])

(def with-profile-matrix-str
  (->> matrix-profiles keys (concat [:dev]) sort (map #(str "+" (name %))) (str/join ":")))

(defproject io.github.frenchy64/instrument-defprotocol "1.0.0-SNAPSHOT"
  :description "Tools to instrument defprotocol."
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :url "https://github.com/frenchy64/instrument-defprotocol"
  :dev {:dependencies [[org.clojure/clojure "1.10.3"]]}
  :profiles ~(assoc matrix-profiles
                    :gen-doc {:jvm-opts ["--add-opens" "java.base/java.lang=ALL-UNNAMED"]})
  :aliases {"matrix" ["with-profile" ~with-profile-matrix-str]}
  :deploy-repositories [["snapshot" {:url "https://clojars.org/repo"
                                     :username :env/clojars_user
                                     :password  :env/clojars_token
                                     :sign-releases false}]
                        ["release" {:url "https://clojars.org/repo"
                                    :username :env/clojars_user
                                    :password  :env/clojars_token
                                    :sign-releases false}]]
  :release-tasks [["clean"]
                  ["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["shell" "./scripts/regen-latest-version-info.sh"]
                  ["shell" "./scripts/regen-selmer.sh"]
                  ["shell" "./scripts/deploy-doc.sh"]
                  ["deploy" "release"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :codox {:source-uri "https://github.com/frenchy64/instrument-defprotocol/blob/{git-commit}/{filepath}#L{line}"}
  :plugins [[lein-codox "0.10.7"]
            [lein-shell "0.5.0"]
            [lein-pprint "1.3.2"]]
  :repl-options {:init-ns io.github.frenchy64.instrument-defprotocol.clj})
