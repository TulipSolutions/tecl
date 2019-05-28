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
from tulipsolutions.api.pub import ticker_pb2, ticker_pb2_grpc


def public_ticker_service_stream_tickers(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = ticker_pb2_grpc.PublicTickerServiceStub(channel)

    # Create a request for streaming the tickers for all markets
    request = ticker_pb2.StreamTickersRequest()

    try:
        # Make the request synchronously and iterate over the received orderbook entries
        for response in stub.StreamTickers(request):
            print(response)
            # CODEINCLUDE-END-MARKER: ref-code-example-request
            parse_and_print(response)
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    except grpc.RpcError as e:
        print("PublicTickerService.StreamTickers error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request


def parse_and_print(response):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    print(
        "{} {} mid_price {} best_buy_price: {} best_buy_size: {} best_sell_price: {} best_sell_size: {} open: {}, "
        "high: {} low: {} close: {} volume_base: {} volume_quote: {}".format(
            type(response).__name__,
            orders_pb2.Market.Name(response.market),
            response.mid_price,
            response.best_buy_price,
            response.best_buy_size,
            response.best_sell_price,
            response.best_sell_size,
            response.daily_open,
            response.daily_high,
            response.daily_low,
            response.daily_close,
            response.daily_volume_base,
            response.daily_volume_quote,
        )
    )
    # CODEINCLUDE-END-MARKER: ref-code-example-response
