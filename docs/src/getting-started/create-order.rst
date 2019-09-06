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

        .. codeinclude:: /examples/go/hello_exchange/main.go
            :marker-id: getting-started-create-order-authentication

    .. tab-container:: Java

        The code snippet below shows the main method from the previous tutorial along with code required for
        user and message authentication.
        Note that the mac provided to the interceptor must be an HMAC-SHA256.
        The snippet also creates a new client side stub and runs a to-be-created function :code:`createOrder`.

        .. codeinclude:: /examples/java/hello_exchange/HelloExchange.java
            :marker-id: getting-started-create-order-authentication

    .. tab-container:: Node

        The code snippet below shows the file from the previous tutorial along with code required for user and message
        authentication.
        The snippet also creates a new client side stub.

        .. codeinclude:: /examples/node/hello_exchange/index.js
            :marker-id: getting-started-create-order-authentication

    .. tab-container:: Python

        The code snippet below shows the main block from the previous tutorial along with code required for
        user and message authentication.
        The snippet also creates a new client side stub and runs a to-be-created function :code:`create_order` in
        a separate thread.

        .. codeinclude:: /examples/python/hello_exchange/hello_exchange.py
            :marker-id: getting-started-create-order-authentication


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

        .. codeinclude:: /examples/go/hello_exchange/main.go
            :marker-id: getting-started-create-order-request

        Run the example with :code:`$ bazelisk run //examples/go/hello_exchange`

        This concludes the getting started tutorial, the example code can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/tree/master/examples/go/hello_exchange>`__.

    .. tab-container:: Java

        .. codeinclude:: /examples/java/hello_exchange/HelloExchange.java
            :marker-id: getting-started-create-order-request

        Run the example with :code:`$ bazelisk run //examples/java/hello_exchange`

        This concludes the getting started tutorial, the example code can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/tree/master/examples/java/hello_exchange>`__.

    .. tab-container:: Node

        .. codeinclude:: /examples/node/hello_exchange/index.js
            :marker-id: getting-started-create-order-request

        Run the example with :code:`$ bazelisk run //examples/node/hello_exchange`

        This concludes the getting started tutorial, the example code can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/tree/master/examples/node/hello_exchange>`__.

    .. tab-container:: Python

        .. codeinclude:: /examples/python/hello_exchange/hello_exchange.py
            :marker-id: getting-started-create-order-request

        Run the example with :code:`$ bazelisk run //examples/python/hello_exchange`

        This concludes the getting started tutorial, the example code can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/tree/master/examples/python/hello_exchange>`__.
