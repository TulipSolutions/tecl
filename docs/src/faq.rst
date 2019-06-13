.. _faq:

Frequently Asked Questions
==========================

.. _faq_supported_price_precision:

What's the supported price precision?
-------------------------------------

The current price precision (resolution) depends on the market and can be requested using the
:protobuf:servicemethod:`tulipsolutions.api.pub.GetMarketDetails`.

In case the given price precision is too high, the error given
(:protobuf:message:`tulipsolutions.api.common.InvalidPricePrecision` or
:protobuf:message:`tulipsolutions.api.common.InvalidAmountPrecision`) will include the maximum number of digits allowed
for this market.

Please note that the precision for :ref:`Public Orderbook <tulip_api_public_orderbook>` entries is specified using
:protobuf:enum:`tulipsolutions.api.pub.Precision`.

.. _faq_trade_order_id_unique:

Are IDs globally unique?
------------------------

Event IDs (``event_id`` fields) for orders and trades such as the ones used in
:protobuf:message:`tulipsolutions.api.priv.PrivateTrade`, :protobuf:message:`tulipsolutions.api.priv.OrderEvent` and
:protobuf:message:`tulipsolutions.api.pub.PublicTrade` are guaranteed unique.

Order IDs (``order_id`` fields) are set by end-users and are unique *per market*.
