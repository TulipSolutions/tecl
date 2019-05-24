.. _getting_started_bazel:

Getting started using Bazel
===========================

This tutorial walks you through the basic functionality of the Tulip Exchange API.
It consists of three parts: setting up the project, streaming the public orderbook and setting an order.
The code for this example can be found here: https://github.com/tulipsolutions/tecl/tree/master/examples.

In this section of the tutorial we compile the tulip client API from the protobuf sources using :term:`Bazel`.

Create a new project
~~~~~~~~~~~~~~~~~~~~~~
Set up a new project by creating a new Bazel workspace.
A workspace is a directory that contains source files, as well as symbolic links to directories that contain the build
outputs.
More information about workspaces can be found
`here <https://docs.bazel.build/versions/master/build-ref.html#workspace>`__

Copy the following content into a file named WORKSPACE in the root of your project.

.. content-tabs::

    .. tab-container:: Go

        .. literalinclude:: /examples/go/WORKSPACE.doc
            :language: python

    .. tab-container:: Java

        .. literalinclude:: /examples/java/WORKSPACE.doc
            :language: python

    .. tab-container:: Node

        .. code-block:: python

            workspace(name = "tulip_api_example_node")
             load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

             http_archive(
                 name = "nl_tulipsolutions_protocol",
                 sha256 = "<checksum>",
                 strip_prefix = "tecl-<version>",
                 url = "https://github.com/tulipsolutions/tecl/archive/<version>.tar.gz"
             )

             load(
                 "@nl_tulipsolutions_tecl//bazel:repositories.bzl",
                  nl_tulipsolutions_protocol_repositories = "repositories",
              )

             nl_tulipsolutions_protocol_repositories()

             load("@build_bazel_rules_nodejs//:defs.bzl", "npm_install")

             npm_install(
                 name = "npm",
                 package_json = "//:package.json",
                 package_lock_json = "//:package-lock.json",
             )

        Node.js projects rely on released artifacts available through npm, therefore run

        .. code-block:: bash

             $ npm install --save google-protobuf grpc @tulipsolutions/tecl


    .. tab-container:: Python

        .. literalinclude:: /examples/python/WORKSPACE.doc
            :language: python

Create the hello_exchange package
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
For the hello exchange example, first create a new Bazel package.
A package is a collection of related files and a specification of the dependencies among them
(more information `here <https://docs.bazel.build/versions/master/build-ref.html#packages>`__).
The relations are declared through *rules* in the BUILD.bazel file: they state the exact relationship between a set of
input and a set of output files.

In the project root directory create a new directory called "hello_exchange".
In this directory create a BUILD.bazel file with the following content.

.. content-tabs::

    .. tab-container:: Go

        .. literalinclude:: /examples/go/hello_exchange/BUILD.bazel
            :lines: 7-
            :language: python

        The code above shows a rule that builds a go library from a :code:`main.go` file and a set of dependencies.
        The dependencies are also rules, but refer to other workspaces, they include:

        * The set of generated go gRPC bindings for the Tulip Exchange API (based on the
          `protobuf definitions <https://github.com/tulipsolutions/tecl>`__).
        * `The Go implementation of gRPC <https://github.com/grpc/grpc-go>`_.

        A second rule uses the library as input to build an executable.

    .. tab-container:: Java

        .. literalinclude:: /examples/java/hello_exchange/BUILD.bazel
            :lines: 7-
            :language: python

        The code above shows a rule that builds a Java binary from a :code:`HelloExchange.java` file and a set of
        dependencies.
        The dependencies are also rules, but refer to other workspaces, they consist of:

        *   The set of generated Java gRPC bindings for the Tulip Exchange API
            (based on the `protobuf definitions <https://github.com/tulipsolutions/tecl>`__).
        *   `The Java implementation of gRPC <https://github.com/grpc/grpc-java>`_.

    .. tab-container:: Node

        .. code-block:: python

            load("@build_bazel_rules_nodejs//:defs.bzl", "nodejs_binary")

            filegroup(
                name = "srcs",
                srcs = glob(["*.js"]),
            )

            nodejs_binary(
                name = "hello_exchange",
                data = [
                    ":srcs",
                    "@npm//tulip_api",
                ],
                entry_point = "tulip_api_example_node/hello_exchange/index.js",
            )

        The code above shows one rule that aggregates Javascript files.
        A second rule uses these aggregated files and the Tulip Exchange NPM dependency to build an executable.

    .. tab-container:: Python

        .. literalinclude:: /examples/python/hello_exchange/BUILD.bazel
            :lines: 9-
            :language: python

        The code above shows a rule that builds a Python binary from a :code:`hello_exchange.py` file and a set of
        dependencies.
        The dependencies are also rules, but refer to other workspaces, they consist of:

        *   `The Protobuf PyPI dependency <https://pypi.org/project/protobuf>`_.
        *   `The six PyPI dependency <https://pypi.org/project/six/>`_.
        *   `The Python implementation of gRPC <https://github.com/grpc/grpc/tree/master/src/python/grpcio>`_.
        *   The set of generated Python gRPC bindings for the Tulip Exchange API
            (based on the `protobuf definitions <https://github.com/tulipsolutions/tecl>`__).

Now that the project setup is done, continue with :ref:`getting_started_streaming_public_orderbook`.
