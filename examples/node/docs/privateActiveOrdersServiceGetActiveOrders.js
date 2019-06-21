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

const orders_pb = require('@tulipsolutions/tecl/common/orders_pb');
const order_pb = require('@tulipsolutions/tecl/priv/order_pb');
const order_grpc = require('@tulipsolutions/tecl/priv/order_grpc_pb');

function privateActiveOrdersServiceGetActiveOrders(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new order_grpc.PrivateActiveOrdersServiceClient(host, credentials);

  // Create a request for all your active orders
  // no fields are set as it does not have any
  const request = new order_pb.GetActiveOrdersRequest();

  // Add a 1s deadline, and make the request asynchronously
  const deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  const callOptions = Object.assign({ deadline: deadline }, options);
  client.getActiveOrders(request, callOptions, (err, response) => {
    if (err) {
      console.error('PrivateActiveOrdersService.GetActiveOrders error: ' + err.message);
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
  let resultString = util.format('%s\n', 'OrderbookSnapshot');
  response.getOrdersList().forEach(activeOrder => {
    let orderTypeDetail = '';
    switch (activeOrder.getOrderCase()) {
      case order_pb.ActiveOrderStatus.OrderCase.LIMIT_ORDER: {
        const limitOrder = activeOrder.getLimitOrder();
        orderTypeDetail = util.format(
          '%s %f@%f remaining %f',
          Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === limitOrder.getSide()),
          limitOrder.getBaseAmount(),
          limitOrder.getPrice(),
          limitOrder.getBaseRemaining()
        );
        break;
      }
      default:
        orderTypeDetail = 'was removed from orderbook';
    }
    resultString += util.format(
      '\t%s: %s for market %s %s\n',
      'ActiveOrderStatus',
      activeOrder.getOrderId(),
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === activeOrder.getMarket()),
      orderTypeDetail
    );
  });
  console.log(resultString);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateActiveOrdersServiceGetActiveOrders;
