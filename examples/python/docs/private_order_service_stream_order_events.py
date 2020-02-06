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

import grpc
import sys
import time

from tulipsolutions.api.common import orders_pb2
from tulipsolutions.api.priv import order_pb2, order_pb2_grpc


def private_order_service_stream_order_events(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = order_pb2_grpc.PrivateOrderServiceStub(channel)

    start = int(time.time() * 1E9)
    # Create a request for streaming all your order events in the BTC_EUR market that occur after start
    request = order_pb2.StreamOrderEventsRequest(
        search_direction=orders_pb2.FORWARD,
        timestamp_ns=start,
        markets=[orders_pb2.BTC_EUR]
    )

    try:
        # Make the request synchronously and iterate over the received orderbook entries
        for response in stub.StreamOrderEvents(request):
            print(response)
            # CODEINCLUDE-END-MARKER: ref-code-example-request
            parse_and_print(response)
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    except grpc.RpcError as e:
        print("PrivateOrderService.StreamOrderEvents error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request


def parse_and_print(event):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    if event.WhichOneof("event") == "create_order_event":
        create_order_event = event.create_order_event
        if create_order_event.deadline_ns:
            deadline = "deadline @ {}".format(create_order_event.deadline_ns)
        else:
            deadline = "(no deadline)"
        if create_order_event.WhichOneof("order_type") == "create_limit_order":
            limit_order_event = create_order_event.create_limit_order
            print("{}: Event {} order {} on market {} {} {}@{} {}\n".format(
                type(limit_order_event).__name__,
                event.event_id,
                event.order_id,
                orders_pb2.Market.Name(event.market),
                orders_pb2.Side.Name(limit_order_event.side),
                limit_order_event.base_amount,
                limit_order_event.price,
                deadline,
            ))
        if create_order_event.WhichOneof("order_type") == "create_market_order":
            market_order_event = create_order_event.create_market_order
            print("{}: Event {} order {} on market {} {} {} {}\n".format(
                type(market_order_event).__name__,
                event.event_id,
                event.order_id,
                orders_pb2.Market.Name(event.market),
                orders_pb2.Side.Name(market_order_event.side),
                market_order_event.base_amount,
                deadline,
            ))
    elif event.WhichOneof("event") == "fill_order_event":
        fill_order_event = event.fill_order_event
        print("{}: Event {} order {} on market {} {} {}@{}\n".format(
            type(fill_order_event).__name__,
            event.event_id,
            event.order_id,
            orders_pb2.Market.Name(event.market),
            orders_pb2.Side.Name(fill_order_event.side),
            fill_order_event.base_amount,
            fill_order_event.price,
        ))
    elif event.WhichOneof("event") == "cancel_order_event":
        cancel_order_event = event.cancel_order_event
        print("{}: Event {} order {} on market {}\n".format(
            type(cancel_order_event).__name__,
            event.event_id,
            event.order_id,
            orders_pb2.Market.Name(event.market),
        ))
    # CODEINCLUDE-END-MARKER: ref-code-example-response
