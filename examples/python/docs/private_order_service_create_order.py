# Copyright 2019 Tulipsolutions B.V.
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

import time

import grpc

from tulipsolutions.api.common import orders_pb2
from tulipsolutions.api.priv import order_pb2, order_pb2_grpc


def private_order_service_create_order(channel):
    stub = order_pb2_grpc.PrivateOrderServiceStub(channel)

    # Create a request for a new order with an orderId that is the nanos since unix epoch
    order_id = round(time.time() * 1E9)
    request = order_pb2.CreateOrderRequest(
        market=orders_pb2.BTC_EUR,
        limit_order=order_pb2.LimitOrderRequest(
            side=orders_pb2.BUY,
            base_amount=1.0,
            price=3000.0,
        ),
        tonce=order_id,
    )

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.CreateOrder(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PrivateOrderService.CreateOrder error: " + str(e), file=sys.stderr)
