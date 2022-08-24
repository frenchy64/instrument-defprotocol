{% do-not-edit-xml-comment %}
# instrument-defprotocol

Utilities to wrap defprotocol.

## Dependency Information

Available on [Clojars](https://clojars.org/com.ambrosebs/{◊project-name◊}).

Leiningen:

```clojure
[com.ambrosebs/{◊project-name◊} "{◊current-version◊}"]
```

Clojure CLI (Maven deps):

```clojure
  :deps {com.ambrosebs/{◊project-name◊}
         {:mvn/version "{◊current-version◊}"}}
```

Clojure CLI (git deps):

```clojure
  ;; requires `clj -X:deps prep` to compile java
  :deps {com.ambrosebs/{◊project-name◊}
         {:git/url "{◊project-url◊}" :git/tag "{◊current-version◊}", :git/sha "{◊short-sha◊}"}}
```

Try it in a REPL:

```clojure
clj -Sdeps '{:deps {com.ambrosebs/{◊project-name◊} {:git/url "{◊project-url◊}", :git/tag "{◊current-version◊}", :git/sha "{◊short-sha◊}"}}}'
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
