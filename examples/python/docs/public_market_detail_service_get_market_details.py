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


def public_market_detail_service_get_market_details(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = market_detail_pb2_grpc.PublicMarketDetailServiceStub(channel)

    # Create a request for the details for all markets
    request = market_detail_pb2.GetMarketDetailsRequest()

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.GetMarketDetails(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PublicMarketDetailService.GetMarketDetails error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request

    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    result_string = "{}\n".format(type(response).__name__)
    for market_detail in response.market_details:
        print(market_detail)
        result_string += (
            "\t{} {} {} base_currency: {}, quote_currency: {} price_reso: {} amount_reso: {} "
            "minimum_base_order_amount: {} maximum_base_order_amount: {} "
            "minimum_quote_order_amount: {} maximum_quote_order_amount: {}\n"
        ).format(
            type(market_detail).__name__,
            orders_pb2.Market.Name(market_detail.market),
            market_detail_pb2.MarketStatus.Name(market_detail.market_status),
            orders_pb2.Currency.Name(market_detail.base),
            orders_pb2.Currency.Name(market_detail.quote),
            market_detail.price_resolution,
            market_detail.amount_resolution,
            market_detail.minimum_base_order_amount,
            market_detail.maximum_base_order_amount,
            market_detail.minimum_quote_order_amount,
            market_detail.maximum_quote_order_amount,
        )
    print(result_string)
    # CODEINCLUDE-END-MARKER: ref-code-example-response
