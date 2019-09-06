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

        .. codeinclude:: /examples/go/hello_exchange/main.go
            :marker-id: getting-started-orderbook-service-init

    .. tab-container:: Java

        In the hello_exchange package create a class :code:`HelloExchange` with a main method.
        In this method, setup the connection to the Tulip Exchange API and create an orderbook client side stub.
        Next, call a to-be-created method :code:`streamOrderbook` that asynchronously streams the public orderbook.
        Finally, wait for user input to kill the program.

        .. codeinclude:: /examples/java/hello_exchange/HelloExchange.java
            :marker-id: getting-started-orderbook-service-init

    .. tab-container:: Node

        In the hello_exchange package create new file :code:`index.js`.
        Import the required modules and create an orderbook client side stub.

        .. codeinclude:: /examples/node/hello_exchange/index.js
            :marker-id: getting-started-orderbook-service-init

    .. tab-container:: Python

        In the hello_exchange package create new file :code:`hello_exchange.py` with a main block.
        In this main block, setup the connection to the Tulip Exchange backend, create an orderbook client side stub and
        run a to-be-created function :code:`stream_orderbook` in a separate thread.

        .. codeinclude:: /examples/python/hello_exchange/hello_exchange.py
            :marker-id: getting-started-orderbook-service-init


Request the public orderbook stream
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Now that the framework is in place we can create a function that does the actual work: create a request, call the
client stub with it, and print the responses.

.. content-tabs::

    .. tab-container:: Go

        .. codeinclude:: /examples/go/hello_exchange/main.go
            :marker-id: getting-started-orderbook-service-request

        Run the example with :code:`$ bazelisk run //examples/go/hello_exchange`

    .. tab-container:: Java

        .. codeinclude:: /examples/java/hello_exchange/HelloExchange.java
            :marker-id: getting-started-orderbook-service-request

        Run the example with :code:`$ bazelisk run //examples/java/hello_exchange`

    .. tab-container:: Node

        .. codeinclude:: /examples/node/hello_exchange/index.js
            :marker-id: getting-started-orderbook-service-request

        Run the example with :code:`$ bazelisk run //examples/node/hello_exchange`

    .. tab-container:: Python

        .. codeinclude:: /examples/python/hello_exchange/hello_exchange.py
            :marker-id: getting-started-orderbook-service-request

        Run the example with :code:`$ bazelisk run //examples/python/hello_exchange`
