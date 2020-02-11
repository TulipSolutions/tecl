# Tulip Exchange Client Library

*This project is currently in **alpha**. The API should be considered unstable and likely to change.*

The Tulip Exchange API is a [gRPC](https://grpc.io/) API for interacting with the Tulip Exchange backend.
Use it to place and cancel orders, monitor the orderbook, stream trades and any other interactions.
The API is defined in a set of [protobuf files](https://developers.google.com/protocol-buffers/) that are used to
generate language specific stubs.
In addition to these stubs, the Tulip Exchange Client Library also contains some helper functions to aid authentication.

## Documentation

* [Getting started](https://demo.tulipsolutions.nl/docs/getting-started/setup-project.html)
* [Public API Reference documentation](https://demo.tulipsolutions.nl/docs/about-public-api.html)
* [Private API Reference documentation](https://demo.tulipsolutions.nl/docs/about-private-api.html)
* [Tulip Exchange API Authentication](https://demo.tulipsolutions.nl/docs/authentication.html)
* [FAQ](https://demo.tulipsolutions.nl/docs/faq.html)

## Usage with Bazel

To install Bazel itself:

* [Install Bazel's dependencies](https://docs.bazel.build/install.html)
* [Install Bazelisk](https://github.com/bazelbuild/bazelisk/releases) (optional, but recommended)

### Java specifics

For Java examples, *either*:

* [Install JDK 8 or higher locally](https://adoptopenjdk.net/); or
* Let Bazel download a JDK for you by passing the following command line arguments when invoking Bazel(isk).

Please see the [Getting started section](https://demo.tulipsolutions.nl/docs/getting-started/setup-project.html) for
more details steps.

### Python specifics

For the Python examples: [Install Python 2 and 3](https://www.python.org/downloads/). If you're using your operating
system's package manager, also install the development packages.
<sub>Python 2 is still required unfortunately, due to an issue with the gRPC-Bazel rules
([#21963](https://github.com/grpc/grpc/issues/21963)).</sub>

### Go and Node

To run Go (Golang) or Node (Javascript) examples, all other dependencies are managed by Bazel.

### Running the examples

Run an example with `bazelisk run examples/<lang>/<example>`, e.g.:

    bazelisk run //examples/go/hello_exchange

Your language not supported? [Submit a feature request](https://github.com/tulipsolutions/tecl/issues) 
or [compile](#compile-from-source) the bindings for your language by yourself.

### Include in another Bazel project

Include this project as an external dependency in your `WORKSPACE`

    load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

    git_repository(
        name = "nl_tulipsolutions_tecl",
        commit = "<commit ID>",
        remote = "https://github.com/tulipsolutions/tecl.git",
    )

Depend on the API parts that you need in your build files, for example:

    java_library(
        name = "lib",
        srcs = glob(["*.java"]),
        deps = ["@nl_tulipsolutions_tecl//tulipsolutions/api/priv:order_jvm_grpc"],
    )

## Usage with other build tools

An alternative to using Bazel is to compile stubs in your language direct from the protobuf definitions.
[gRPC.io](https://grpc.io/docs/) contains a list of officially supported languages.
Visit our [getting started from source](https://demo.tulipsolutions.nl/docs/getting-started/from-source.html) page,
select your preferred language and follow the tutorial.

## Testing against the Exchange

For the purpose of verifying your client side code, we provide a number of options:

* Online demo; create an account and use your private JWT obtained there (see API access).
* Online mocked gRPC; returning bogus stateless data with a shared dummy JWT that works out of the box from a clone
  (this is the default).
* Offline mocked gRPC; run a gRPC server locally with just one command. See [`mockgrpc/README.md`](mockgrpc/README.md).
