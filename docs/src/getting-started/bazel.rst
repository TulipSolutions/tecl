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
      https://github.com/bazelbuild/bazelisk/releases/download/v1.3.0/bazelisk-linux-amd64
   $ sudo chmod +x /usr/local/bin/bazelisk

Now verify that you can run Bazel and see the expected latest version printed by running ``$ bazelisk version``.

Install dependencies
~~~~~~~~~~~~~~~~~~~~

.. content-tabs::

    .. tab-container:: Go

        For using Go in this example, no additional dependencies are required.

    .. tab-container:: Java

        For using Java in this example, you can choose between installing a JDK locally or let Bazel download and use
        that. TECL was tested with the OpenJDK JDK LTS versions 8 and 11.

        The quickest way is to let Bazel download a Java JDK 11 for you; skip over to the next steps in
        "Create a new project".

        If you choose to install a local JDK, several options exist. For Debian/Ubuntu Linux this would mean:

        .. code-block:: bash

           $ sudo apt install openjdk-11-jdk-headless
           $ sudo update-alternatives --config java  # select OpenJDK 11 path
           $ sudo update-alternatives --config javac  # select OpenJDK 11 path

        For other operating systems or more options, you could have a look at the
        [AdoptOpenJDK project](https://adoptopenjdk.net/).

    .. tab-container:: Node

        For using Node in this example, no additional dependencies are required.

    .. tab-container:: Python

        For using Python in this example, you will need to install both Python 2, Python 3, the development
        headers of both versions and Python 3 distutils. For e.g. on Debian/Ubuntu Linux this would mean:

        .. code-block:: bash

           $ sudo apt install python python-dev python3 python3-dev python3-distutils

        .. tip::
           The reason for a Python 2 requirement is an issue with the gRPC-Python rules for Bazel
           (`#21963 <https://github.com/grpc/grpc/issues/21963>`_). Despite this requirement, the example code and your
           application can be written in pure Python 3.

Create a new project
~~~~~~~~~~~~~~~~~~~~

Set up a new project by creating a new Bazel workspace.
A workspace is a directory that contains source files, as well as symbolic links to directories that contain the build
outputs.
More information about workspaces can be found
`here <https://docs.bazel.build/versions/master/build-ref.html#workspace>`__

Copy the following content into a file named ``WORKSPACE`` in the root of your project.

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

.. content-tabs::

    .. tab-container:: Java

        To let Bazel manage a Java JDK 11 for you, create a file ``.bazelrc`` in the root of your project with the
        following content.

        .. code-block::

             # Use Bazel's "remote" JDK 11.
             build --javabase=@bazel_tools//tools/jdk:remote_jdk11
             build --host_javabase=@bazel_tools//tools/jdk:remote_jdk11
             build --host_java_toolchain=@bazel_tools//tools/jdk:toolchain_vanilla
             build --java_toolchain=@bazel_tools//tools/jdk:toolchain_vanilla

Create the hello_exchange package
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
For the hello exchange example, first create a new Bazel package.
A package is a collection of related files and a specification of the dependencies among them
(more information `here <https://docs.bazel.build/versions/master/build-ref.html#packages>`__).
The relations are declared through *rules* in the ``BUILD.bazel`` file: they state the exact relationship between a set
of input and a set of output files.

In the project root directory create a new directory called "hello_exchange".
In this directory create a ``BUILD.bazel`` file with the following content.

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
