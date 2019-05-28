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


def public_ticker_service_get_tickers(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = ticker_pb2_grpc.PublicTickerServiceStub(channel)

    # Create a request for the tickers for all markets
    request = ticker_pb2.GetTickersRequest()

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.GetTickers(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PublicTickerService.GetTickers error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request

    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    result_string = "{}\n".format(type(response).__name__)
    for tick in response.ticks:
        result_string += (
            "\t{} {} mid_price {} best_buy_price: {} best_buy_size: {} best_sell_price: {} best_sell_size: {} "
            "open: {}, high: {} low: {} close: {} volume_base: {} volume_quote: {}\n"
        ).format(
            type(tick).__name__,
            orders_pb2.Market.Name(tick.market),
            tick.mid_price,
            tick.best_buy_price,
            tick.best_buy_size,
            tick.best_sell_price,
            tick.best_sell_size,
            tick.daily_open,
            tick.daily_high,
            tick.daily_low,
            tick.daily_close,
            tick.daily_volume_base,
            tick.daily_volume_quote,
        )
    print(result_string)
    # CODEINCLUDE-END-MARKER: ref-code-example-response
