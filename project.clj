(defproject io.github.frenchy64/instrument-defprotocol "1.0.0-SNAPSHOT"
  :description "Tools to instrument defprotocol."
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :url "https://github.com/frenchy64/instrument-defprotocol"
  :dependencies [[org.clojure/clojure "1.10.3"]]
  :profiles {:gen-doc
             {:jvm-opts ["--add-opens" "java.base/java.lang=ALL-UNNAMED"]}}
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
