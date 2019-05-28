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
from tulipsolutions.api.pub import orderbook_pb2, orderbook_pb2_grpc


def public_orderbook_service_get_orderbook(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = orderbook_pb2_grpc.PublicOrderbookServiceStub(channel)

    # Create a request for the BTC_EUR orderbook, with the greatest precision, largest length,
    # and highest update frequency
    # See TBD for semantics of Precision and Length
    request = orderbook_pb2.GetOrderbookRequest(
        market=orders_pb2.BTC_EUR,
        precision=orderbook_pb2.P0,
        length=orderbook_pb2.NUM_ENTRIES_25,
    )

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.GetOrderbook(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PublicOrderbookService.GetOrderbook error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request

    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    result_string = "{}\n".format(type(response).__name__)
    for orderbook_entry in response.entries:
        result_string += "\t{} {} {} orders @ {} total {}\n".format(
            type(orderbook_entry).__name__,
            orders_pb2.Side.Name(orderbook_entry.side),
            orderbook_entry.orders_at_price_level,
            orderbook_entry.price_level,
            orderbook_entry.amount,
        )
    print(result_string)
    # CODEINCLUDE-END-MARKER: ref-code-example-response
