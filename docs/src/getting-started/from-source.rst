.. _getting_started_from_source:

Getting started from source
===========================

This is the first part of the Getting Started tutorial.

It consists of three parts: setting up the project, streaming the public orderbook and setting an order.
The code for this example can be found on the :abbr:`TECL (Tulip Exchange Client Library)` GitHub project, in the
`'examples' folder <https://github.com/tulipsolutions/tecl/tree/master/examples>`__.

In this section of the tutorial we compile TECL directly from the (Protobuf) sources. Although not the fastest way to
get started, it allows you to benefit from the latest API spec and generate code for any language with gRPC support.

.. content-tabs::

    .. tab-container:: Go

        The steps in this section very briefly describe how to compile language bindings from the protobuf definitions,
        for a more in-depth introduction to gRPC, we refer to the
        `official documentation <https://grpc.io/docs/tutorials/basic/go.html>`__

    .. tab-container:: Java

        The steps in this section very briefly describe how to compile language bindings from the protobuf definitions,
        for a more in-depth introduction to gRPC, we refer to the
        `official documentation <https://grpc.io/docs/tutorials/basic/java.html>`__

    .. tab-container:: Node

        The steps in this section very briefly describe how to compile language bindings from the protobuf definitions,
        for a more in-depth introduction to gRPC, we refer to the
        `official documentation <https://grpc.io/docs/tutorials/basic/node.html>`__

    .. tab-container:: Python

        The steps in this section very briefly describe how to compile language bindings from the protobuf definitions,
        for a more in-depth introduction to gRPC, we refer to the
        `official documentation <https://grpc.io/docs/tutorials/basic/python.html>`__

Prerequisites
~~~~~~~~~~~~~

.. content-tabs::

    .. tab-container:: Go

        The official Go quickstart offers an excellent
        `'Prerequisites' section <https://grpc.io/docs/quickstart/go/#prerequisites>`__.
        In that tutorial, three steps are essential.

        First, use the following command to install gRPC.

        .. code-block:: bash

            $ go get -u google.golang.org/grpc

        Next, install the protoc compiler that is used to generate gRPC service code.
        The simplest way to do this is to download pre-compiled binaries for your
        platform(protoc-<version>-<platform>.zip) from
        `Protobuf's releases page on GitHub <https://github.com/protocolbuffers/protobuf/releases>`__.

        Finally, install the protoc plugin for Go.

        .. code-block:: bash

            $ go get -u github.com/golang/protobuf/protoc-gen-go

        The compiler plugin, protoc-gen-go, will be installed in $GOBIN, defaulting to $GOPATH/bin.
        It must be in your $PATH for the protocol compiler, protoc, to find it.

        .. code-block:: bash

            $ export PATH=$PATH:$GOPATH/bin

    .. tab-container:: Java

        First, install the protoc compiler that is used to generate gRPC service code.
        The simplest way to do this is to download pre-compiled binaries for your
        platform(protoc-<version>-<platform>.zip) from
        `Protobuf's releases page on GitHub <https://github.com/protocolbuffers/protobuf/releases>`__.

        When using Gradle or Maven, the protoc build plugin can generate the necessary code as part of the build.
        You can refer to this `README <https://github.com/grpc/grpc-java/blob/master/README.md>`__ for how to generate
        code from your own .proto files using these build tools.

        If your build tool does not have a plugin for code generation, simply use protoc with the gRPC Java plugin.
        This plugin is not included in the default protoc binary, but needs to be built from source.
        The instructions can be found `here <https://github.com/grpc/grpc-java/tree/master/compiler>`__.

    .. tab-container:: Node

        There are two ways to generate the code needed to work with protocol buffers in Node.js - one approach uses
        `Protobuf.js <https://github.com/dcodeIO/ProtoBuf.js>`__ to dynamically generate the code at runtime,
        the other uses code statically generated using the protocol buffer compiler protoc.
        We will be using the latter, but both implementations are compatible with the Tulip Exchange backend.

        NPM provides a package that wraps the protobuf compiler and a plugin for generating gRPC Python stubs.
        Install it with:

        .. code-block:: bash

            $ npm install grpc-tools

    .. tab-container:: Python

        PyPI provides a package that wraps the protobuf compiler and a plugin for generating gRPC Python stubs.
        Install it with:

        .. code-block:: bash

            $ pip install grpcio-tools

Now that we have Protobuf and gRPC compilers installed, clone the TECL repository on your local
machine. This tutorial assumes a git submodule of the repository in the project root directory, but you're free to use
other methods of integration.

Create one with the following commands:

.. code-block:: bash

    $ git submodule add https://github.com/tulipsolutions/tecl.git vendor/tecl
    $ git submodule update --init

Generating gRPC bindings
~~~~~~~~~~~~~~~~~~~~~~~~

.. content-tabs::

    .. tab-container:: Go

        The following command generates the complete set of Go code for the Tulip Exchange API.
        It looks up protobuf files in the folder :code:`vendor/tecl`, compiles the Go stubs and
        places the output in the current directory.

        .. code-block:: bash

            $ find vendor/tecl/{tulipsolutions,third_party/protoc-gen-validate/validate} -name "*.proto" \
                  -exec protoc --proto_path=vendor/tecl/third_party/protoc-gen-validate \
                               --proto_path=vendor/tecl \
                               --go_out=plugins=grpc:. '{}' \;

    .. tab-container:: Java

        The following command generates the complete set of Java classes for the Tulip Exchange API.
        It looks up protobuf files in the folder :code:`vendor/tecl`, compiles the Java
        stubs and places the output in :code:`src/main/java`.

        .. code-block:: bash

            $ mkdir -p src/main/java
            $ find vendor/tecl/{tulipsolutions,third_party/protoc-gen-validate/validate} -name "*.proto" -print0 \
              | xargs -0 protoc --proto_path=vendor/tecl/third_party/protoc-gen-validate \
                                --proto_path=vendor/tecl \
                                --plugin=protoc-gen-grpc-java=<path-to-grpc-java-protoc-plugin> \
                                --java_out=src/main/java \
                                --grpc-java_out=src/main/java

    .. tab-container:: Node

        The following command generates the complete set of Node.js code for the Tulip Exchange API.
        It looks up protobuf files in the folder :code:`vendor/tecl`, compiles the Node.js stubs and
        places the output in the current directory.

        .. code-block:: bash

            $ find vendor/tecl/{tulipsolutions,third_party/protoc-gen-validate/validate} -name "*.proto" -print0 \
              | xargs -0 node_modules/grpc-tools/bin/protoc \
                  --proto_path=vendor/tecl/third_party/protoc-gen-validate \
                  --proto_path=vendor/tecl \
                  --plugin=protoc-gen-grpc-node="node_modules/grpc-tools/bin/grpc_node_plugin" \
                  --js_out=import_style=commonjs,binary:. \
                  --grpc-node_out=import_style=commonjs,binary:.

    .. tab-container:: Python

        The following command generates the complete set of Python code for the Tulip Exchange API.
        It looks up protobuf files in the folder :code:`vendor/tecl`, compiles the Python stubs and
        places the output in the current directory.

        .. code-block:: bash

            $ find vendor/tecl/{tulipsolutions,third_party/protoc-gen-validate/validate} -name "*.proto" -print0 \
              | xargs -0 python -m grpc_tools.protoc \
                  --proto_path=vendor/tecl/third_party/protoc-gen-validate \
                  --proto_path=vendor/tecl \
                  --python_out=:. \
                  --grpc_python_out=:.

Include the auth module and gRPC dependencies
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. content-tabs::

    .. tab-container:: Go

        In addition to the generated gRPC bindings, the TECL Go-helpers make authentication easier.
        To be able to follow the rest of the tutorial, make sure to include these files.

        Either copy the contents of the :code:`vendor/tecl/go` directory to
        :code:`github.com/tulipsolutions/tecl` or make sure your build tool registers this directory as a sources
        directory.

        .. code-block:: bash

            $ rsync -a --prune-empty-dirs --include "*/"  --include="*.go" --exclude="*" vendor/tecl/go/ github.com/tulipsolutions/tecl

    .. tab-container:: Java

        In addition to the generated gRPC bindings, the TECL Java-helpers make authentication easier.
        To be able to follow the rest of the tutorial, make sure to include these files.

        Either copy the contents of the :code:`vendor/tecl/java` directory to :code:`src/main/java` or make sure your build tool
        registers this directory as a sources directory.

        .. code-block:: bash

            $ rsync -a --prune-empty-dirs --include "*/"  --include="*.java" --exclude="*" vendor/tecl/java/ src/main/java

        Lastly, add the `grpc-stub <https://mvnrepository.com/artifact/io.grpc/grpc-stub>`__,
        `grpc-protobuf <https://mvnrepository.com/artifact/io.grpc/grpc-protobuf>`__ and
        `grpc-netty <https://mvnrepository.com/artifact/io.grpc/grpc-netty>`__ dependencies to your project.

    .. tab-container:: Node

        In addition to the generated gRPC bindings, the TECL Node.js-helpers make authentication easier.
        To be able to follow the rest of the tutorial, make sure to include these files.

        Either copy the contents of the :code:`vendor/tecl/node` directory to `tulipsolutions/api` or make sure your build tool
        registers this directory as a sources directory.

        .. code-block:: bash

            $ rsync -a --prune-empty-dirs --include "*/"  --include="*.js" --exclude="*" vendor/tecl/node/ tulipsolutions/api

        Lastly, add the `grpc <https://www.npmjs.com/package/grpc/>`__ and
        `protobuf <https://www.npmjs.com/package/google-protobuf>`__ dependencies to your project with:

        .. code-block:: bash

            $ npm install --save grpc google-protobuf

    .. tab-container:: Python

        In addition to the generated gRPC bindings, the TECL Python-helpers make authentication easier.
        To be able to follow the rest of the tutorial, make sure to include these files.

        .. code-block:: bash

            $ rsync -a --prune-empty-dirs --include "*/"  --include="*.py" --exclude="*" vendor/tecl/python/ .

        Either copy the contents of the :code:`vendor/tecl/python` directory to your project or make sure to register
        this directory as a sources directory.

        Lastly, add the `grpcio <https://pypi.org/project/grpcio/>`__ and
        `protobuf <https://pypi.org/project/protobuf/>`__ dependencies to your project with:

        .. code-block:: bash

            $ pip install grpcio protobuf

Now that the project setup is done, continue with :ref:`getting_started_streaming_public_orderbook`.
