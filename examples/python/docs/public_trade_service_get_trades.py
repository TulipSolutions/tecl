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
from tulipsolutions.api.pub import trade_pb2, trade_pb2_grpc


def public_trade_service_get_trades(channel):
    stub = trade_pb2_grpc.PublicTradeServiceStub(channel)

    # Create a request for the most recent trades in the BTC_EUR market
    request = trade_pb2.GetPublicTradesRequest(
        market=orders_pb2.BTC_EUR,
    )

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.GetTrades(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PublicTradeService.GetTrades error: " + str(e), file=sys.stderr)
