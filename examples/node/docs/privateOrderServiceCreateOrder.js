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

function privateOrderServiceCreateOrder(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new order_grpc.PrivateOrderServiceClient(host, credentials);

  // CODEINCLUDE-BEGIN-MARKER: authentication-request
  // Create a request for a new order with an orderId that is the nanos since unix epoch
  const orderId = Date.now() * 1000000;
  const limitOrderRequest = new order_pb.LimitOrderRequest();
  limitOrderRequest.setSide(orders_pb.Side.BUY);
  limitOrderRequest.setBaseAmount(1.0);
  limitOrderRequest.setPrice(3000);
  const request = new order_pb.CreateOrderRequest();
  request.setMarket(orders_pb.Market.BTC_EUR);
  request.setTonce(orderId.toString());
  request.setLimitOrder(limitOrderRequest);
  // CODEINCLUDE-END-MARKER: authentication-request

  // Add a 1s deadline, and make the request asynchronously
  const deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  const callOptions = Object.assign({ deadline: deadline }, options);
  client.createOrder(request, callOptions, (err, response) => {
    if (err) {
      console.error('PrivateOrderService.CreateOrder error: ' + err.message);
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
  let orderTypeDetail = '';
  switch (response.getOrderTypeCase()) {
    case order_pb.CreateOrderResponse.OrderTypeCase.LIMIT_ORDER: {
      const order = response.getLimitOrder();
      orderTypeDetail = util.format(
        '%s %f@%f',
        Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === order.getSide()),
        order.getBaseAmount(),
        order.getPrice()
      );
      break;
    }
    case order_pb.CreateOrderResponse.OrderTypeCase.MARKET_ORDER: {
      const order = response.getMarketOrder();
      orderTypeDetail = util.format(
        '%s %f',
        Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === order.getSide()),
        order.getBaseAmount()
      );
      break;
    }
    default:
      orderTypeDetail = 'Should not be empty!';
  }
  let deadline = '(no deadline)';
  if (response.getDeadlineNs() != 0) {
    deadline = util.format('deadline @ %d', response.getDeadlineNs());
  }
  console.log(
    util.format(
      '%s: %s for market %s %s %s',
      'CreateOrderResponse',
      response.getOrderId(),
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === response.getMarket()),
      orderTypeDetail,
      deadline
    )
  );
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateOrderServiceCreateOrder;
