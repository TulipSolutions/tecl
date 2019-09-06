.. _getting_started_bazel:

Getting started using Bazel
===========================

This tutorial walks you through the basic functionality of the Tulip Exchange API.
It consists of three parts: setting up the project, streaming the public orderbook and setting an order.
The code for this example can be found here: https://github.com/tulipsolutions/tecl/tree/master/examples.

In this section of the tutorial we compile the tulip client API from the protobuf sources using :term:`Bazel`.

Installing Bazel (Bazelisk)
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Please see the `Bazel project page on installation <https://docs.bazel.build/versions/master/install.html>`_ for the
Bazel prerequisites.
Note that you do not have to install Bazel itself.
For e.g. on Debian/Ubuntu Linux this would mean:

.. code-block:: bash

   $ sudo apt install pkg-config zip g++ zlib1g-dev unzip python3

`Bazelisk <https://github.com/bazelbuild/bazelisk>`_ is a simple wrapper to always download and invoke the right
version of Bazel for the project, determined by the ``.bazelversion`` file in the workspace root.
The steps following in this guide assume an installation of Bazelisk somewhere in your PATH, e.g. ``/usr/local/bin``:

.. code-block:: bash

   $ sudo curl -Lo /usr/local/bin/bazelisk \
      https://github.com/bazelbuild/bazelisk/releases/download/v1.0/bazelisk-linux-amd64
   $ sudo chmod +x /usr/local/bin/bazelisk

Now verify that you can run Bazel and see the expected latest version printed by running ``$ bazelisk version``.

Create a new project
~~~~~~~~~~~~~~~~~~~~
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

        .. literalinclude:: /examples/node/WORKSPACE.doc
            :language: python

        Node.js projects rely on released artifacts fetched locally by Yarn, therefore run

        .. code-block:: bash

             $ bazelisk run @nodejs//:bin/yarn -- install

    .. tab-container:: Python

        .. literalinclude:: /examples/python/WORKSPACE.doc
            :language: python

Create the hello_exchange package
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
For the hello exchange example, first create a new Bazel package.
A package is a collection of related files and a specification of the dependencies among them
(more information `here <https://docs.bazel.build/versions/master/build-ref.html#packages>`__).
The relations are declared through *rules* in the ``BUILD.bazel`` file: they state the exact relationship between a set
of input and a set of output files.

In the project root directory create a new directory called "hello_exchange".
In this directory create a BUILD.bazel file with the following content.

.. content-tabs::

    .. tab-container:: Go

        .. codeinclude:: /examples/go/hello_exchange/BUILD.bazel
            :marker-id: getting-started-bazel-package

        The code above shows a rule that builds a go library from a :code:`main.go` file and a set of dependencies.
        The dependencies are also rules, but refer to other workspaces, they include:

        * The set of generated go gRPC bindings for the Tulip Exchange API (based on the
          `protobuf definitions <https://github.com/tulipsolutions/tecl>`__).
        * `The Go implementation of gRPC <https://github.com/grpc/grpc-go>`_.

        A second rule uses the library as input to build an executable.

    .. tab-container:: Java

        .. codeinclude:: /examples/java/hello_exchange/BUILD.bazel
            :marker-id: getting-started-bazel-package

        The code above shows a rule that builds a Java binary from a :code:`HelloExchange.java` file and a set of
        dependencies.
        The dependencies are also rules, but refer to other workspaces, they consist of:

        *   The set of generated Java gRPC bindings for the Tulip Exchange API
            (based on the `protobuf definitions <https://github.com/tulipsolutions/tecl>`__).
        *   `The Java implementation of gRPC <https://github.com/grpc/grpc-java>`_.

    .. tab-container:: Node

        .. codeinclude:: /examples/node/hello_exchange/BUILD.bazel
            :marker-id: getting-started-bazel-package

        The code above shows a rule that builds a node binary from an :code:`index.js` file and
        the Tulip Exchange Client Library (tecl) NPM package.

    .. tab-container:: Python

        .. codeinclude:: /examples/python/hello_exchange/BUILD.bazel
            :marker-id: getting-started-bazel-package

        The code above shows a rule that builds a Python binary from a :code:`hello_exchange.py` file and a set of
        dependencies.
        The dependencies are also rules, but refer to other workspaces, they consist of:

        *   `The Protobuf PyPI dependency <https://pypi.org/project/protobuf>`_.
        *   `The six PyPI dependency <https://pypi.org/project/six/>`_.
        *   `The Python implementation of gRPC <https://github.com/grpc/grpc/tree/master/src/python/grpcio>`_.
        *   The set of generated Python gRPC bindings for the Tulip Exchange API
            (based on the `protobuf definitions <https://github.com/tulipsolutions/tecl>`__).

Now that the project setup is done, continue with :ref:`getting_started_streaming_public_orderbook`.
