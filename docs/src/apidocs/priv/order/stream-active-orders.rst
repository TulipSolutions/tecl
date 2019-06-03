Stream active orders
====================

:protobuf:include:`tulipsolutions.api.priv.StreamActiveOrders`

:protobuf:include:`tulipsolutions.api.priv.StreamActiveOrdersRequest`

:protobuf:include:`tulipsolutions.api.priv.ActiveOrderStatus`

:protobuf:include:`tulipsolutions.api.priv.LimitOrderStatus`

.. content-tabs::
   :class: code-example-responsive

   .. tab-container:: Go

      .. codeinclude:: /examples/go/docs/private_active_orders_service_stream_active_orders.go
         :marker-id: ref-code-example-request
         :caption: Request

      .. codeinclude:: /examples/go/docs/private_active_orders_service_stream_active_orders.go
         :marker-id: ref-code-example-response
         :caption: Example response handling

   .. tab-container:: Java

      .. codeinclude:: /examples/java/docs/PrivateActiveOrdersServiceStreamActiveOrders.java
         :marker-id: ref-code-example-request

   .. tab-container:: Node

      .. codeinclude:: /examples/node/docs/privateActiveOrdersServiceStreamActiveOrders.js
         :marker-id: ref-code-example-request

   .. tab-container:: Python

      .. codeinclude:: /examples/python/docs/private_active_orders_service_stream_active_orders.py
         :marker-id: ref-code-example-request
