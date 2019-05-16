.. _getting_started_streaming_public_orderbook:

Part 2: Public orderbook streaming
==================================

In the second part of the getting-started-tutorial we make a streaming call to the public orderbook and print the
returned values.

Initialize a connection to the orderbook service
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. content-tabs::

    .. tab-container:: Go

        In the hello_exchange package create a file :code:`main.go` with a main function.
        In this function, setup the connection to the Tulip Exchange backend and create an orderbook client side stub.
        Next, run a to-be-created function :code:`streamOrderbook` in a goroutine.
        Finally, wait for user input to kill the program.

        .. literalinclude:: /examples/go/hello_exchange/main.go
            :lines: 35-37,97,107,115-124,126,128-131,133,135-
            :language: go

    .. tab-container:: Java

        In the hello_exchange package create a class :code:`HelloExchange` with a main method.
        In this method, setup the connection to the Tulip Exchange API and create an orderbook client side stub.
        Next, call a to-be-created method :code:`streamOrderbook` that asynchronously streams the public orderbook.
        Finally, wait for user input to kill the program.

        .. literalinclude:: /examples/java/hello_exchange/HelloExchange.java
            :lines: 112-113,131-134,137,141,143-149
            :dedent: 4
            :language: java

    .. tab-container:: Node

        In the hello_exchange package create new file :code:`index.js`.
        Import the required modules and create an orderbook client side stub.

        .. literalinclude:: /examples/node/hello_exchange/index.js
            :lines: 31-33,45
            :language: js

    .. tab-container:: Python

        In the hello_exchange package create new file :code:`hello_exchange.py` with a main block.
        In this main block, setup the connection to the Tulip Exchange backend, create an orderbook client side stub and
        run a to-be-created function :code:`stream_orderbook` in a separate thread.

        .. literalinclude:: /examples/python/hello_exchange/hello_exchange.py
            :lines: 72-75,90,92,93-94
            :language: python


Request the public orderbook stream
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Now that the framework is in place we can create a function that does the actual work: create a request, call the
client stub with it, and print the responses.

.. content-tabs::

    .. tab-container:: Go

        .. literalinclude:: /examples/go/hello_exchange/main.go
            :lines: 39-69
            :language: go

        Run the example with :code:`$ bazel run //examples/go/hello_exchange`

    .. tab-container:: Java

        .. literalinclude:: /examples/java/hello_exchange/HelloExchange.java
            :lines: 48-76
            :dedent: 4
            :language: java

        Run the example with :code:`$ bazel run //examples/java/hello_exchange`

    .. tab-container:: Node

        .. literalinclude:: /examples/node/hello_exchange/index.js
            :lines: 48-66
            :language: js

        Run the example with :code:`$ bazel run //examples/node/hello_exchange`

    .. tab-container:: Python

        .. literalinclude:: /examples/python/hello_exchange/hello_exchange.py
            :lines: 32-47
            :language: python

        Run the example with :code:`$ bazel run //examples/python/hello_exchange`
