<!-- DO NOT EDIT! Instead, edit `dev/resources/root-templates/README.md` and run `./script/regen-selmer.sh` -->
# instrument-defprotocol

Utilities to wrap defprotocol.

## Dependency Information

Available on [Clojars](https://clojars.org/com.ambrosebs/instrument-defprotocol).

Leiningen:

```clojure
[com.ambrosebs/instrument-defprotocol "1.0.0-SNAPSHOT"]
```

Clojure CLI (Maven deps):

```clojure
  :deps {com.ambrosebs/instrument-defprotocol
         {:mvn/version "1.0.0-SNAPSHOT"}}
```

Clojure CLI (git deps):

```clojure
  ;; requires `clj -X:deps prep` to compile java
  :deps {com.ambrosebs/instrument-defprotocol
         {:git/url "https://github.com/frenchy64/instrument-defprotocol" :git/tag "1.0.0-SNAPSHOT", :git/sha "4862b97"}}
```

Try it in a REPL:

```clojure
clj -Sdeps '{:deps {com.ambrosebs/instrument-defprotocol {:git/url "https://github.com/frenchy64/instrument-defprotocol", :git/tag "1.0.0-SNAPSHOT", :git/sha "4862b97"}}}'
```

## License

```
Copyright © 2022 Ambrose Bonnaire-Sergeant

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
```
