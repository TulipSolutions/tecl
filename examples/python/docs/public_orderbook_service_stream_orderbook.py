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


def public_orderbook_service_stream_orderbook(channel):
    stub = orderbook_pb2_grpc.PublicOrderbookServiceStub(channel)

    # Create a request for streaming the BTC_EUR orderbook, with the greatest precision, largest length,
    # and highest update frequency
    # See TBD for semantics of Precision, Length and Frequency
    request = orderbook_pb2.StreamOrderbookRequest(
        market=orders_pb2.BTC_EUR,
        precision=orderbook_pb2.P3,
        length=orderbook_pb2.NUM_ENTRIES_100,
        frequency=orderbook_pb2.BEST_EFFORT,
    )

    try:
        # Make the request synchronously and iterate over the received orderbook entries
        for response in stub.StreamOrderbook(request):
            print(response)
    except grpc.RpcError as e:
        print("PublicOrderbookService.StreamOrderbook error: " + str(e), file=sys.stderr)
