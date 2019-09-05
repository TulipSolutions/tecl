# Copyright 2019 Tulip Solutions B.V.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from __future__ import print_function

import sys

import grpc

from tulipsolutions.api.common import orders_pb2
from tulipsolutions.api.priv import order_pb2, order_pb2_grpc


def private_active_orders_service_get_active_orders(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = order_pb2_grpc.PrivateActiveOrdersServiceStub(channel)

    # Create a request for all your active orders
    # no fields are set as it does not have any
    request = order_pb2.GetActiveOrdersRequest()

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.GetActiveOrders(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PrivateActiveOrdersService.GetActiveOrders error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request

    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    result_string = "{}\n".format(type(response).__name__)
    for activeOrder in response.orders:
        if activeOrder.WhichOneof("order") == "limit_order":
            order_type_detail = "{} {}@{} remaining {}".format(
                orders_pb2.Side.Name(activeOrder.limit_order.side),
                activeOrder.limit_order.base_amount,
                activeOrder.limit_order.price,
                activeOrder.limit_order.base_remaining,
            )
        # Note that market orders do not show in active orderbook.
        else:
            order_type_detail = "was removed from orderbook"
        if activeOrder.deadline_ns:
            deadline = "deadline @ {}".format(activeOrder.deadline_ns)
        else:
            deadline = "(no deadline)"
        result_string += "\t{}: {} for market {} {} {}\n".format(
            type(activeOrder).__name__,
            activeOrder.order_id,
            orders_pb2.Market.Name(activeOrder.market),
            order_type_detail,
            deadline,
        )
    print(result_string)
    # CODEINCLUDE-END-MARKER: ref-code-example-response
