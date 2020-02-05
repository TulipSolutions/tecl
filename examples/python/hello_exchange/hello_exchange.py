#!/usr/bin/env python
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


import base64
import time
from concurrent.futures import ThreadPoolExecutor

import grpc
try:
    from tecl.python.tulipsolutions.api.auth import jwt_interceptor
except ImportError:
    from tulipsolutions.api.auth import jwt_interceptor
try:
    from tecl.python.tulipsolutions.api.auth import message_authentication_interceptor
except ImportError:
    from tulipsolutions.api.auth import message_authentication_interceptor

from tulipsolutions.api.common import orders_pb2
from tulipsolutions.api.priv import order_pb2
from tulipsolutions.api.priv import order_pb2_grpc
from tulipsolutions.api.pub import orderbook_pb2
from tulipsolutions.api.pub import orderbook_pb2_grpc


# CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-request
def stream_orderbook(public_orderbook_service_stub):
    # Create a request for the BTC_EUR orderbook, with the greatest precision, largest length,
    # and highest update frequency
    stream_orderbook_request = orderbook_pb2.StreamOrderbookRequest(
        market=orders_pb2.BTC_EUR,
        precision=orderbook_pb2.P3,
        length=orderbook_pb2.NUM_ENTRIES_100,
        frequency=orderbook_pb2.BEST_EFFORT,
    )

    try:
        # Print the received orderbook entries
        for response in public_orderbook_service_stub.StreamOrderbook(stream_orderbook_request):
            print("Received message %s" % response)
    except grpc.RpcError as e:
        print("PublicOrderbookServie.StreamOrderbook error: " + str(e))
# CODEINCLUDE-END-MARKER: getting-started-orderbook-service-request


# CODEINCLUDE-BEGIN-MARKER: getting-started-create-order-request
def create_order(private_order_service_stub):
    # Create a request for a new order with an orderId that is the nanos since unix epoch
    order_id = round(time.time() * 1E9)
    create_order_request = order_pb2.CreateOrderRequest(
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
        response = private_order_service_stub.CreateOrder(create_order_request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PrivateOrderService.CreateOrder error: " + str(e))
# CODEINCLUDE-END-MARKER: getting-started-create-order-request


# Subscribe to a public orderbook stream and set a new order
# CODEINCLUDE-BEGIN-MARKER: getting-started-create-order-authentication
# CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
if __name__ == '__main__':
    creds = grpc.ssl_channel_credentials()

    with grpc.secure_channel('mockgrpc.test.tulipsolutions.nl:443', creds) as channel:
        # CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
        # Create a SHA256 HMAC with the base64 decoded 'secret' string as its key
        dummy_secret = base64.standard_b64decode("secret==")
        dummy_jwt = "eyJraWQiOiI2YzY4OTIzMi03YTcxLTQ3NGItYjBlMi1lMmI1MzMyNDQzOWUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0In0.IL9QJQl55qn3oPsT7sFa7iwd5g1GsEQVr0IO7gCe1UmQdjT7jCIc-pUfjyYUgptPR8HBQl5ncXuBnxwjdXqOMwW1WhPmi_B3BRHQh3Sfu0zNXqKhkuz2-6DffXK1ek3DmK1NpaSikXtg2ruSQ4Uk5xHcnxmXY_SwEij0yot_JRKYEs-0RbyD5Z4jOFKcsbEW46WQmiWdgG3PUKiJT5TfdFd55JM55BwzSOdPIP1S_3dQ4VTDo30mWqAs1KaVbcPqCQmjT1PL0QScTp4w8-YPDcajcafIj98ve9LUoLBLraCIAX34D-hOxu643h9DoG2kIPFfZyXbkDTiUKOl7t-Ykg"  # noqa E501

        # Create an interceptor that signs messages with the provided secret.
        # Only messages to the private API that have a 'signed' field will be signed.
        message_auth_interceptor = message_authentication_interceptor.create(dummy_secret)
        # Create an interceptor that adds a JWT token when a request to a private service is made.
        jwt_interceptor = jwt_interceptor.create(dummy_jwt)
        # Add interceptors to all requests over the channel
        channel = grpc.intercept_channel(channel, jwt_interceptor, message_auth_interceptor)

        # Construct clients for accessing PublicOrderbookService and PrivateOrderService using the existing connection.
        # Add a deadline to all requests to the PrivateOrderService
        # CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
        public_orderbook_service_stub = orderbook_pb2_grpc.PublicOrderbookServiceStub(channel)
        # CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
        private_order_service_stub = order_pb2_grpc.PrivateOrderServiceStub(channel)
        # CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init

        with ThreadPoolExecutor(max_workers=2) as executor:
            executor.submit(stream_orderbook, public_orderbook_service_stub)
            # CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
            executor.submit(create_order, private_order_service_stub)
