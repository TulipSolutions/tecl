.. _getting_started_create_order:

Part 3: Create a new order
==========================

In the last part of the getting-started-tutorial we add authentication to the connection and create a new order.

.. _getting_started_authentication:

Authentication
~~~~~~~~~~~~~~

Unlike streaming the public orderbook, creating an order is an action that requires user authentication.
This is done using JWT tokens (*Note: the authentication spec is far from finished and will change*).
:term:`TECL` contains an interceptor that adds such a token to the metadata of a request. Instantiating the interceptor
with the token and applying it to the connection is all that is required.

In addition to user authentication, some actions (such as creating an order) require an extra level of authentication,
referred to as message or request authentication.
Request authentication is done by signing the raw bytes of a message using a secret and sending that signature along
with the request.
Similar to user authentication, TECL contains an interceptor for this as well.
See :ref:`authentication_request_authentication` for a more in-depth explanation of how request authentication is
implemented.

.. content-tabs::

    .. tab-container:: Go

        The code snippet below shows the main function from the previous tutorial along with code required for
        user and message authentication.
        Note that the mac provided to the interceptor must be an HMAC-SHA256.
        The snippet also creates a new client side stub and runs a to-be-created function :code:`createOrder` in a
        goroutine.

        .. literalinclude:: /examples/go/hello_exchange/main.go
            :lines: 95-
            :language: go

    .. tab-container:: Java

        The code snippet below shows the main method from the previous tutorial along with code required for
        user and message authentication.
        Note that the mac provided to the interceptor must be an HMAC-SHA256.
        The snippet also creates a new client side stub stub and runs a to-be-created function :code:`createOrder`.

        .. literalinclude:: /examples/java/hello_exchange/HelloExchange.java
            :lines: 112-149
            :dedent: 4
            :language: java

    .. tab-container:: Node

        The code snippet below shows the file from the previous tutorial along with code required for user and message
        authentication.
        The snippet also creates a new client side stub stub.

        .. literalinclude:: /examples/node/hello_exchange/index.js
            :lines: 27-47
            :language: js

    .. tab-container:: Python

        The code snippet below shows the main block from the previous tutorial along with code required for
        user and message authentication.
        The snippet also creates a new client side stub stub and runs a to-be-created function :code:`create_order` in
        a separate thread.

        .. literalinclude:: /examples/python/hello_exchange/hello_exchange.py
            :lines: 72-
            :language: python


Make a request for a new order
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Because authentication is dealt with on a connection level, creating a new order is not much different from streaming
the public orderbook.
Aside from the required interceptors, the only difference is that :code:`CreateOrder` returns a single response,
after which the call is over.

Add the following snippet to the example code.
Note that a :protobuf:message:`tulipsolutions.api.priv.CreateOrderRequest` takes a :term:`tonce`, this value becomes the
order ID with which the order can be cancelled.

.. content-tabs::

    .. tab-container:: Go

        .. literalinclude:: /examples/go/hello_exchange/main.go
            :lines: 71-94
            :language: go

        Run the example with :code:`$ bazel run //examples/go/hello_exchange`

        This concludes the getting started tutorial, the example code can be found `on Github <https://github.com/tulipsolutions/tecl/tree/master/examples/go/hello_exchange>`__.

    .. tab-container:: Java

        .. literalinclude:: /examples/java/hello_exchange/HelloExchange.java
            :lines: 78-110
            :dedent: 4
            :language: java

        Run the example with :code:`$ bazel run //examples/java/hello_exchange`

        This concludes the getting started tutorial, the example code can be found `on Github <https://github.com/tulipsolutions/tecl/tree/master/examples/java/hello_exchange>`__.

    .. tab-container:: Node

        .. literalinclude:: /examples/node/hello_exchange/index.js
            :lines: 68-
            :language: js

        Run the example with :code:`$ bazel run //examples/node/hello_exchange`

        This concludes the getting started tutorial, the example code can be found `on Github <https://github.com/tulipsolutions/tecl/tree/master/examples/node/hello_exchange>`__.

    .. tab-container:: Python

        .. literalinclude:: /examples/python/hello_exchange/hello_exchange.py
            :lines: 50-68
            :language: python

        Run the example with :code:`$ bazel run //examples/python/hello_exchange`

        This concludes the getting started tutorial, the example code can be found `on Github <https://github.com/tulipsolutions/tecl/tree/master/examples/python/hello_exchange>`__.
