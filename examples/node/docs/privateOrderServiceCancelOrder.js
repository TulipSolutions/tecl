/**
 * Copyright 2019 Tulip Solutions B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
const util = require('util');

const order_pb = require('@tulipsolutions/tecl/priv/order_pb');
const orders_pb = require('@tulipsolutions/tecl/common/orders_pb');
const order_grpc = require('@tulipsolutions/tecl/priv/order_grpc_pb');

function privateOrderServiceCancelOrder(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new order_grpc.PrivateOrderServiceClient(host, credentials);

  // Create a request for an order cancellation with a tonce that is the nanos since unix epoch
  const tonce = Date.now() * 1000000;
  const request = new order_pb.CancelOrderRequest();
  request.setOrderId('1');
  request.setMarket(orders_pb.Market.BTC_EUR);
  request.setTonce(tonce.toString());

  // Add a 1s deadline, and make the request asynchronously
  const deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  const callOptions = Object.assign({ deadline: deadline }, options);
  client.cancelOrder(request, callOptions, (err, response) => {
    if (err) {
      console.error('PrivateOrderService.CancelOrder error: ' + err.message);
    }
    if (response) {
      console.log(response.toObject());
      // CODEINCLUDE-END-MARKER: ref-code-example-request
      parseAndPrint(response);
      // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    }
  });
  // CODEINCLUDE-END-MARKER: ref-code-example-request
}

function parseAndPrint(response) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
  console.log(
    util.format(
      '%s: %s for %s',
      'CancelOrderResponse',
      response.getOrderId(),
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === response.getMarket())
    )
  );
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateOrderServiceCancelOrder;
