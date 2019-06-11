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
from tulipsolutions.api.priv import trade_pb2, trade_pb2_grpc


def private_trade_service_stream_trades(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = trade_pb2_grpc.PrivateTradeServiceStub(channel)

    # Create a request for streaming all your trades in the BTC_EUR that occur after initiation of the request
    request = trade_pb2.StreamPrivateTradesRequest(
        market=orders_pb2.BTC_EUR,
    )

    try:
        # Make the request synchronously and iterate over the received orderbook entries
        for response in stub.StreamTrades(request):
            print(response)
    except grpc.RpcError as e:
        print("PrivateTradeService.StreamTrades error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request
