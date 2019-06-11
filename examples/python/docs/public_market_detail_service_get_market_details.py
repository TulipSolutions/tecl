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
