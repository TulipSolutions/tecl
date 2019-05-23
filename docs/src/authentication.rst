.. _authentication:

Authentication
==============

This section describes authentication with the Tulip Exchange backend in detail.
Most users will not need to know these details as :term:`TECL` includes interceptors that do most of the work for the
supported languages.
We publish the details of authentication in case someone wants to implement his or her own own interceptors and for
auditability.

Depending on the type of request, authentication is performed on one of the following levels:

#.  None: none of the RPCs in the public API require authentication
#.  User: all RPCs in the private API require a user to be authenticated
#.  User and Request: some RPCs mutate account state in such a critical way that we need to authenticate the creator of
    a particular message in addition to the user being authenticated.


.. _authentication_user_authentication:

User authentication
-------------------

All requests to the private API require a user to be authenticated.
A request becomes authenticated when it as a valid `JWT token <https://tools.ietf.org/html/rfc7519>`__ in its metadata.
A JWT token consists of a number of claims and a signature and is created by the authenticating party.
An attacker can impersonate a user when it obtains the token, therefore it is important to treat this as secret.
At the time of writing, these tokens have a very long lifetime and can be obtained manually from us in case you want to
play with the API.

As stated before the token is sent as metadata along with a gRPC request, meaning that on-the-wire it is sent as an
HTTP/2 header.
All traffic to the Tulip Solutions API, including the HTTP/2 headers, is encrypted.
As a result, the JWT token can never be read by a party listening to network traffic.
The header key used for sending JWT tokens is the ASCII string ``authorization``.

Token contents
~~~~~~~~~~~~~~

All of our API examples include the following dummy JWT token.

.. codeinclude:: /examples/go/hello_exchange/main.go
    :marker-id: dummy-jwt-token-line

This consists of two ``.`` delimited base64 encoded JSON structs and the signature bytes.
By decoding the string at https://jwt.io/ we obtain the following algorithm and token type:

.. code-block:: json

    {
      "kid": "6c689232-7a71-474b-b0e2-e2b53324439e",
      "typ": "JWT",
      "alg": "RS256"
    }

Meaning that:

*   It is a JWT token
*   The token is signed with an RSA 256 (and thus asymmetric) key
*   The signature is created by a key with identifier ``6c689232-7a71-474b-b0e2-e2b53324439e``

The payload block consists of the the following struct.

.. code-block:: json

    {
      "sub": "1234"
    }

Meaning that the JWT token authenticates a user with ID ``1234``.
The dummy token does not contain any additional claims nor does it contain an expiration date.
Tokens used in the production environment contain both.

Token overhead
~~~~~~~~~~~~~~

The token in the previous section is 456 character ASCII string that should be in the metadata of every authenticated
request.
Fortunately, the request is an HTTP/2 request which benefits from HPACK compression.
Describing the specifics of HPACK goes beyond the scope of this document,
for an introduction we refer you to https://blog.cloudflare.com/hpack-the-silent-killer-feature-of-http-2/.

As a result of this compression the dummy JWT token introduces a 411 byte overhead on the first authenticated request.
On the next request however, the token overhead is reduced to a single byte because it will be cached thanks to HPACK.
In practice the overhead may become a few bytes, and the token will have to be resent every 100 or so requests,
depending on the requests being made.

.. _authentication_request_authentication:

Request authentication
----------------------

A number of RPC calls, CreateOrder for instance, mutate account state in such a critical way that we find
authentication using a JWT token is not enough.
For these calls we require the creator of the particular request to be authenticated in order to:

* Ensure the request was not tampered with during transit
* Prevent replay of the message
* Obtain additional evidence of the request origin
* Make user consent auditable at a later point in time.

The sent request data for these calls is wrapped in the :protobuf:message:`tulipsolutions.api.priv.Signed` and includes
a unique number (nonce or :term:`tonce`), a signature drawn from the raw request bytes, and the raw request bytes
themselves.
Each request is signed individually with a secret known only to the user and the Tulip Exchange backend.
At the time of writing, the signature is created using a HMAC-SHA256 algorithm and a long lived key.
A secret can be obtained manually from us in case you want to play with the API.

As stated in the introduction of this page, the logic for signing a message is encapsulated in a message authentication
interceptor, included in the Tulip Exchange API code.
We go over it step-by-step to better demonstrate the functionality.

Request authentication starts by creating a request that contains a tonce.
The tonce should be the number of nano seconds since unix epoch with a 5 second error margin.
It should also be a unique value: a user may send the same tonce only once.
The snippet below shows a :protobuf:message:`tulipsolutions.api.priv.CreateOrderRequest` being created with a tonce.
Note that in this implementation, the user may only send one message every millisecond due to the uniqueness constraint.

.. content-tabs::

    .. tab-container:: Go

        .. codeinclude:: /examples/go/docs/private_order_service_create_order.go
            :marker-id: authentication-request

    .. tab-container:: Java

        .. codeinclude:: /examples/java/docs/PrivateOrderServiceCreateOrder.java
            :marker-id: authentication-request

    .. tab-container:: Node

        .. codeinclude:: /examples/node/docs/privateOrderServiceCreateOrder.js
            :marker-id: authentication-request

    .. tab-container:: Python

        .. codeinclude:: /examples/python/docs/private_order_service_create_order.py
            :marker-id: authentication-request

After calling the rpc stub with the created request, the proto message passes through the message
authentication interceptor.
This interceptor transforms the message for you.
What happens under water, is the following:

#. Serialize the message to bytes
#. Sign the bytes with the provided key
#. Create a new message of the same type with only the raw message bytes and the signature
#. Forward the newly created message to the next interceptor


.. content-tabs::

    .. tab-container:: Go

        The code for the Go interceptor can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/blob/master/go/auth/message_authentication.go>`__.

    .. tab-container:: Java

        The code for the Java interceptor can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/blob/master/java/nl/tulipsolutions/api/auth/MessageAuthClientInterceptor.java>`__.

    .. tab-container:: Node

        The code for the Node.js interceptor can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/blob/master/node/auth/index.js>`__.

    .. tab-container:: Python

        The code for the Python interceptor can be found
        `on GitHub <https://github.com/tulipsolutions/tecl/blob/master/python/tulipsolutions/api/auth/message_authentication_interceptor.py>`__.

Verifying your implementation
-----------------------------

For the purpose of verifying your client side code, we provide a mock gRPC service hosted at
https://mockgrpc.test.tulipsolutions.nl.
The service responds on all published RPC methods and verifies both user and request authentication if applicable.
For user authentication simply use the dummy JWT token published above and in the examples.
For request authentication, decode the base64 encoded string: ``secret==`` and use it to create an HMAC-SHA256 key.
Use this key to sign the message in the manner explained in :ref:`authentication_request_authentication`.
