# Protocol documentation


## Documentation framework using Sphinx & protodoc

* [Sphinx][sphinx-website] is a widely-used modular documentation framework.
* `protobufdomain` is a self-developed extension for Sphinx which implements a
  custom [Domain][sphinx-domains] for Protobuf object, to make it 'aware'
  of the different types, handles cross-references and 're-includes'.
* `protodoc` is a self-developed plugin for the Protobuf compiler, to output the
  Protobuf object descriptions in reStructuredText for use in the protobufdomain.
* `contentui` is a self-developed plugin to show/hide content based on a
  page-wide selector. We use this to show the relevant programming language
  instructions or code for the selection given.

Additionally, some glue logic is self-developed to *Bazelify* all of it in
`rules_protodoc` and `rules_sphinx`.

At this time, we're limited to the use of Python 2.x with Bazel, and blocks an
upgrade to Sphinx 2.x, as that version is Python 3.5+ only.

[sphinx-website]: https://www.sphinx-doc.org/
[sphinx-domains]: https://www.sphinx-doc.org/en/1.8/usage/restructuredtext/domains.html


## How to generate documentation

    $ bazel build //docs

The above takes care of generating reStructuredText from the `.proto` files
using protodoc, merging the output with the documentation pages in `docs/src/`
and invoking `sphinx-build` to build HTML output.

You can then open the `index.html` with your browser on the `bazel-bin`
directory printed by Bazel. E.g.:

    $ firefox $(bazel info bazel-bin)/docs/html/index.html

---
**Note**

> The protocol buffer files (`*.proto`) are parsed by protodoc, and is made
available build-time by Bazel rule magic in the `protodoc/` path
relative to the `src` source directory for Sphinx pages. See
[Add/remove Protobuf files for docs](#addremove-protobuf-files-for-docs) for
how to control which files are sourced.

---

## Protodoc documentation how-to


### Leading comments are docs

Comments on the files should be placed in the top and a title can be given with
the `protodoc-title` annotation, e.g.:

    // [#protodoc-title: My API]
    // My documentation about this proto...

Protobuf comments above the message, field, enum, enum value, service, service
method are included as well. E.g.:

```proto
// Query for messages.
message MyMessage {
    // The User ID for ...
    int32 user_id = 1;
    // Maximum number of trades returned
    int32 limit = 2;
}
```

---
**Note**

> Comments following the declaration are *not* parsed currently.
>
> ```proto
> message MyMessage {
>     int32 user_id = 1; // only positive! (but no-one will be able to read this...)
> }
> ```

---

### Support for Envoy's protoc validate plugin

We use Envoy's Protobuf compiler extension [protoc-gen-validate][pgv] to specify
certain requirements on messages and fields. Limited support for this is
included in Protodoc. Currently only the a field requirement is marked as such.
E.g.:

    common.Market market = 1 [(validate.rules).message.required = true];

Will render as:

> market:  (common.MarketValue, *required*)

[pgv]: https://github.com/envoyproxy/protoc-gen-validate


### Comments are in reStructuredText

All is parsed as [reStructuredText][rst] (also referred to as *reST* or *rst*
sometimes) and therefore can use common structures. This includes support for
all the extensive features available like tables, code blocks, etc.

For a quick demonstration on what's possible, the Sphinx Read The Docs theme
has put up some showcase: [source][rtd-theme-showcase-source] |
[rendered][rtd-theme-showcase-rendered].

[rst]: http://docutils.sourceforge.net/rst.html
[rtd-theme-showcase-source]: https://raw.githubusercontent.com/rtfd/sphinx_rtd_theme/master/docs/demo/demo.rst
[rtd-theme-showcase-rendered]: https://sphinx-rtd-theme.readthedocs.io/en/latest/demo/demo.html


### Cross-references

All entities will have a label assigned which can be referenced to from any
other document.

Suppose `tulipsolutions/api/pub/my.proto` contains:

```proto
// Query for messages.
message MyMessageRequest {
    // The request ID.
    int32 request_id = 1;
}
```

You can then refer to this message using Protobuf cross-reference
reStructuredText role (`:protobuf:<objtype>:`), e.g.:

```proto
// Result for messages in a response to
// :protobuf:message:`tulipsolutions.api.pub.MyMessageRequest`.
message MyMessageResponse {
    // ...
}
```

Also, this way you can refer from/to any other document in the (broader)
documentation project, such as a 'Getting started' page referring to specific
API endpoint pages and vice versa.

---
**Note**

> Always make sure to include the fully qualified name of the object, because the
protobufdomain is not yet aware of a "package" scope and the "current package"
you're in.

---

Instead of referring to a certain object, you can also re-include it in the
output at any point with the special `:protobuf:include:` xref-role. For
example, in a `getting-started/example.rst`:

```rest
To make the request, send the `MyMessageRequest` as shown below:

:protobuf:include:`MyMessageRequest`
```


### Non-docs comments

In case you want to write Protobuf comments which should *not* end up in
documentation, use the `comment` annotation, e.g.:

    // [#comment:my comment that does not show up in docs]


### Special Protodoc annotations

* `protodoc-title` - see
  [Leading comments are docs](#leading-comments-are-docs).
* `not-implemented-warn` - adds a warning box with the contents: "Not
  implemented yet".
* `not-implemented-hide` - hide this field/enumvalue/service/method.
* `comment` - see [Non-docs comments](#non-docs-comments)
* `proto-status:<status>` - with `status` set to either `frozen`, `draft` or
  `experimental`, the message/enum/service/field/method will have a warning box
  indicating this status.
* `example-value-ok`, valid on enum values, which will be shown as inline
  examples on message fields of this enum type.


### Add/remove Protobuf files for docs

Adjust the `srcs` labels for the `sphinx_docs` rule in the `BUILD.bazel` file in
this directory to control which Protobuf files are included when invoking Sphinx
to build the documentation.


## Known issues/limitations

* When generating docs from another workspace, the Git revision is taken from
  the consuming workspace repository, not from this. This also blocks generating
  strong Git-native source code links.
* Code examples shown in the sidebar could overlap if these are taller than the
  content in the main body.
* Source code links are not working yet, blocked by:
  * final URL of the public repository + relative path is not known yet.
  * determine revision to link.
* Nesting of object descriptions includes fails with a Sphinx traceback.
* Does not parse custom Protobuf extensions (e.g.
  `extend google.protobuf.EnumValueOptions { [...]`).

Ideas for future improvements in Issue: trading-platform/180.
