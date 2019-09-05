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

function privateActiveOrdersServiceStreamActiveOrders(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new order_grpc.PrivateActiveOrdersServiceClient(host, credentials);

  // Create a request for streaming all your active orders
  // no fields are set as it does not have any
  const request = new order_pb.StreamActiveOrdersRequest();

  // Make the request asynchronously
  const call = client.streamActiveOrders(request, options);
  call.on('data', response => {
    console.log(response.toObject());
    // CODEINCLUDE-END-MARKER: ref-code-example-request
    parseAndPrint(response);
    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  });
  call.on('error', err => {
    console.error('PrivateActiveOrdersService.StreamActiveOrders error: ' + err.message);
  });
  call.on('end', () => {
    console.log('PrivateActiveOrdersService.StreamActiveOrders completed');
  });
  // CODEINCLUDE-END-MARKER: ref-code-example-request
}

function parseAndPrint(response) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
  let orderTypeDetail = '';
  switch (response.getOrderCase()) {
    case order_pb.ActiveOrderStatus.OrderCase.LIMIT_ORDER: {
      const order = response.getLimitOrder();
      orderTypeDetail = util.format(
        '%s %f@%f remaining %f',
        Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === order.getSide()),
        order.getBaseAmount(),
        order.getPrice(),
        order.getBaseRemaining()
      );
      break;
    }
    // Note that market orders do not show in active orderbook.
    default:
      orderTypeDetail = 'was removed from orderbook';
  }
  let deadline = '(no deadline)';
  if (response.getDeadlineNs() != 0) {
    deadline = util.format('deadline @ %d', response.getDeadlineNs());
  }
  console.log(
    util.format(
      '%s: %s for market %s %s %s',
      'ActiveOrderStatus',
      response.getOrderId(),
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === response.getMarket()),
      orderTypeDetail,
      deadline
    )
  );
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateActiveOrdersServiceStreamActiveOrders;
