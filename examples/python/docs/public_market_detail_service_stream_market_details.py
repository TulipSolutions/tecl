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
from tulipsolutions.api.pub import market_detail_pb2, market_detail_pb2_grpc


def public_market_detail_service_stream_market_details(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = market_detail_pb2_grpc.PublicMarketDetailServiceStub(channel)

    # Create a request for streaming the details for all markets
    request = market_detail_pb2.StreamMarketDetailsRequest()

    try:
        # Make the request synchronously and iterate over the received orderbook entries
        for response in stub.StreamMarketDetails(request):
            print(response)
            # CODEINCLUDE-END-MARKER: ref-code-example-request
            parse_and_print(response)
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    except grpc.RpcError as e:
        print("PublicMarketDetailService.StreamMarketDetails error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request


def parse_and_print(response):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    print(
        "{} {} {} base_currency: {}, quote_currency: {} price_reso: {} price_digits: {} "
        "amount_reso: {} amount_digits: {} minimum_base_order_amount: {} maximum_base_order_amount: {} "
        "minimum_quote_order_amount: {} maximum_quote_order_amount: {}".format(
            type(response).__name__,
            orders_pb2.Market.Name(response.market),
            market_detail_pb2.MarketStatus.Name(response.market_status),
            orders_pb2.Currency.Name(response.base),
            orders_pb2.Currency.Name(response.quote),
            response.price_resolution,
            response.price_resolution_digits,
            response.amount_resolution,
            response.amount_resolution_digits,
            response.minimum_base_order_amount,
            response.maximum_base_order_amount,
            response.minimum_quote_order_amount,
            response.maximum_quote_order_amount,
        )
    )
    # CODEINCLUDE-END-MARKER: ref-code-example-response
