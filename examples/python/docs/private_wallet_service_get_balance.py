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
from tulipsolutions.api.priv import wallet_pb2, wallet_pb2_grpc


def private_wallet_service_get_balance(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = wallet_pb2_grpc.PrivateWalletServiceStub(channel)

    # Create a request for your balances for all currencies
    request = wallet_pb2.GetBalanceRequest()

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.GetBalance(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PrivateWalletService.GetBalance error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request

    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    result_string = "{}\n".format(type(response).__name__)
    for balance in response.balance_response:
        result_string += "\t{} {} total: {} locked: {} \n".format(
            type(balance).__name__,
            orders_pb2.Currency.Name(balance.currency),
            balance.total_amount,
            balance.locked_amount,
        )
    print(result_string)
    # CODEINCLUDE-END-MARKER: ref-code-example-response
