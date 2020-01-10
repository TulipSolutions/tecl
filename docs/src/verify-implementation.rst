.. _verify_implementation:

Verify your implementation
==========================

For the purpose of verifying your client side code, we provide several options to test against.

All of these options use the same dummy request message authentication secret.
Decode the base64 encoded string ``secret==`` and use it to create an HMAC-SHA256 key.
Then sign the message in the manner explained in :ref:`authentication_request_authentication`.

Online (hosted) mock gRPC
-------------------------

A mock gRPC service hosted at ``mockgrpc.test.tulipsolutions.nl`` (port 443) responds on all published RPC methods and
verifies both user and request authentication if applicable.
For :ref:`user authentication <authentication>` simply use the dummy JWT token in the code examples.

This service is stateless, providing random (bogus) data, but it does perform server-side input validation.
It may also introduce errors on certain input conditions.
Please refer to the
`mockgrpc README <https://github.com/TulipSolutions/tecl/blob/master/mockgrpc/README.md#error-testing>`__ which
conditions trigger errors.

.. hint::
   The mock gRPC URL does not serve a web page; it only provides gRPC and gRPC-Web.

Locally run (offline) mock gRPC
-------------------------------

Alternatively, you can run the mock gRPC service locally, e.g. for use in an offline environment.
Please refer to the `mockgrpc README <https://github.com/TulipSolutions/tecl/blob/master/mockgrpc/README.md>`__ for
instructions.

Online (hosted) demo
--------------------

A full featured demonstration is up at https://demo.tulipsolutions.nl/ and serves both a web frontend (using gRPC-Web)
and gRPC.
Create a user account and obtain a JWT as API key for the account to get started.

If you want to use the code examples from the :term:`TECL` repository, make sure to replace the server address in the
main file from ``mockgrpc.test.tulipsolutions.nl:443`` to ``demo.tulipsolutions.nl:443`` and the contents of your JWT as
value to the ``dummyJwt`` variable.
