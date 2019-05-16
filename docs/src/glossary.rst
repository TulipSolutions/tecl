Glossary
========

.. glossary::

    Bazel
        Bazel is an open-source build and test tool similar to Make, Maven and Gradle.
        It was initially developed developed internally at Google and open sourced in 2015 [#f1]_.
        Bazel supports projects in multiple languages and builds outputs for multiple platforms.

    gRPC
        High performance RPC framework initially developed at Google and open sourced in 2015 [#f2]_.
        It uses Protocol Buffers as the interface description language and provides features such as authentication,
        bidirectional streaming and flow control, blocking or nonblocking bindings, and cancellation and timeouts [#f3]_.
        Client stubs are generated from protocol definitions for most modern programming languages.
        A list of officially supported languages can be found `here <https://grpc.io/docs/>`__.

    Protobuf
        *language-neutral, platform-neutral, extensible mechanism for serializing structured data – think XML, but
        smaller, faster, and simpler* [#f4]_

        Short for Protocol buffers, it is a binary serialization format, with its structure defined in ``.proto`` files.
        If certain migration rules are followed, the format is backward- and forward-compatible [#f5]_.

    :abbr:`TECL (Tulip Exchange Client Library)`
        Includes Protobuf/gRPC API definitions and helpers for several languages in order to interface with the Tulip
        Exchange. GitHub project: `tulipsolutions/tecl <https://github.com/tulipsolutions/tecl>`_.

    tonce
        A time-based `cryptographic nonce <https://en.wikipedia.org/wiki/Cryptographic_nonce>`_.


..  [#f1] https://opensource.googleblog.com/search/label/bazel
..  [#f2] https://grpc.io/blog/principles
..  [#f3] https://grpc.io/about/
..  [#f4] https://developers.google.com/protocol-buffers/ *retrieved 2019-03-09*
..  [#f5] https://developers.google.com/protocol-buffers/docs/overview
