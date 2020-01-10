Tulip Solutions Exchange Infrastructure API documentation
=========================================================

.. Note: all pages are included in the toctree here as hidden, but we rely on the enabled theme's option 'includehidden'
   to display them in the sidebar.

.. toctree::
   :caption: Getting started toturial
   :maxdepth: 2
   :hidden:

   getting-started/setup-project.rst
   getting-started/streaming-public-orderbook.rst
   getting-started/create-order.rst

.. toctree::
   :caption: Public API
   :maxdepth: 1
   :hidden:

   about-public-api.rst
   apidocs/pub/orderbook/index
   apidocs/pub/trade/index
   apidocs/pub/market_detail/index
   apidocs/pub/ticker/index
   apidocs/pub/ohlc/index

.. toctree::
   :caption: Private API
   :maxdepth: 1
   :hidden:

   about-private-api
   apidocs/priv/order/index
   apidocs/priv/trade/index
   apidocs/priv/wallet/index

.. toctree::
   :caption: Other resources
   :maxdepth: 1
   :hidden:

   authentication.rst
   verify-implementation.rst
   apidocs/all-objects-reference
   FAQ <faq>
   glossary

Welcome to Tulip Solutions' API documentation.

This documentation is aimed at developers who want to use our API in their applications.
You'll find an overview of the contents in the sidebar.
Start by following one of the  :ref:`getting started tutorials <getting_started_bazel>` or delve deeper into the
:ref:`public <about_public_api>` or :ref:`private <about_private_api>` API definition.
At the time of writing we only offer a gRPC API, if you are new to this, we highly recommend having a look at the
official gRPC `quick start <https://grpc.io/docs/quickstart/>`__.

.. tip::
   Have a look at the `online demo <https://demo.tulipsolutions.nl/>`__!
