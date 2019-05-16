Stream active orders
====================

:protobuf:include:`tulipsolutions.api.priv.StreamActiveOrders`

:protobuf:include:`tulipsolutions.api.priv.StreamActiveOrdersRequest`

:protobuf:include:`tulipsolutions.api.priv.ActiveOrderStatus`

:protobuf:include:`tulipsolutions.api.priv.LimitOrderStatus`

.. content-tabs::
   :class: code-example-responsive

   .. tab-container:: Go
      :sidebar:

      .. literalinclude:: /examples/go/docs/private_active_orders_service_stream_active_orders.go
         :lines: 27-53
         :language: go
         :dedent: 1

   .. tab-container:: Java
      :sidebar:

      .. literalinclude:: /examples/java/docs/PrivateActiveOrdersServiceStreamActiveOrders.java
         :lines: 27-46
         :dedent: 8
         :language: java

   .. tab-container:: Node
      :sidebar:

      .. literalinclude:: /examples/node/docs/privateActiveOrdersServiceStreamActiveOrders.js
         :lines: 21-37
         :language: js
         :dedent: 2

   .. tab-container:: Python
      :sidebar:

      .. literalinclude:: /examples/python/docs/private_active_orders_service_stream_active_orders.py
         :lines: 25-36
         :language: python
         :dedent: 4
