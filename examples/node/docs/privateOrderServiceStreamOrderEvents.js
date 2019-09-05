/*
 * Copyright 2019 Tulip Solutions B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
const util = require('util');

const order_pb = require('@tulipsolutions/tecl/priv/order_pb');
const orders_pb = require('@tulipsolutions/tecl/common/orders_pb');
const order_grpc_pb = require('@tulipsolutions/tecl/priv/order_grpc_pb');

function privateOrderServiceStreamOrderEvents(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new order_grpc_pb.PrivateOrderServiceClient(host, credentials);

  const start = Date.now() * 1000000;
  // Create a request for streaming all your trades in the BTC_EUR market that occur after start
  const request = new order_pb.StreamOrderEventsRequest();
  request.addMarkets(orders_pb.Market.BTC_EUR);
  request.setSearchDirection(orders_pb.SearchDirection.FORWARD);
  request.setTimestampNs(start.toString());

  // Make the request asynchronously
  const call = client.streamOrderEvents(request, options);
  call.on('data', response => {
    console.log(response.toObject());
    // CODEINCLUDE-END-MARKER: ref-code-example-request
    parseAndPrint(response);
    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  });
  call.on('error', err => {
    console.error('PrivateOrderService.StreamOrderEvents error: ' + err.message);
  });
  call.on('end', () => {
    console.log('PrivateOrderService.StreamOrderEvents completed');
  });
  // CODEINCLUDE-END-MARKER: ref-code-example-request
}

function parseAndPrint(event) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
  let result;
  switch (event.getEventCase()) {
    case order_pb.OrderEvent.EventCase.CREATE_ORDER_EVENT: {
      const createOrderEvent = event.getCreateOrderEvent();
      let deadline = '(no deadline)';
      if (createOrderEvent.getDeadlineNs() != 0) {
        deadline = util.format('deadline @ %d', createOrderEvent.getDeadlineNs());
      }
      switch (createOrderEvent.getOrderTypeCase()) {
        case order_pb.CreateOrderEvent.OrderTypeCase.CREATE_LIMIT_ORDER: {
          const limitOrderEvent = createOrderEvent.getCreateLimitOrder();
          result = util.format(
            '%s: Event %d order %d on market %s %s %f@%f %s\n',
            'CreateLimitOrderEvent',
            event.getEventId(),
            event.getOrderId(),
            Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === event.getMarket()),
            Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === limitOrderEvent.getSide()),
            limitOrderEvent.getBaseAmount(),
            limitOrderEvent.getPrice(),
            deadline
          );
          break;
        }
        case order_pb.CreateOrderEvent.OrderTypeCase.CREATE_MARKET_ORDER: {
          const marketOrderEvent = createOrderEvent.getCreateMarketOrder();
          result = util.format(
            '%s: Event %d order %d on market %s %s %f %s\n',
            'CreateMarketOrderEvent',
            event.getEventId(),
            event.getOrderId(),
            Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === event.getMarket()),
            Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === marketOrderEvent.getSide()),
            marketOrderEvent.getBaseAmount(),
            deadline
          );
          break;
        }
      }
      break;
    }
    case order_pb.OrderEvent.EventCase.FILL_ORDER_EVENT: {
      const fillOrderEvent = event.getFillOrderEvent();
      result = util.format(
        '%s: Event %d order %d on market %s %s %f@%f\n',
        'FillOrderEvent',
        event.getEventId(),
        event.getOrderId(),
        Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === event.getMarket()),
        Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === fillOrderEvent.getSide()),
        fillOrderEvent.getBaseAmount(),
        fillOrderEvent.getPrice()
      );
      break;
    }
    case order_pb.OrderEvent.EventCase.CANCEL_ORDER_EVENT:
      result = util.format(
        '%s: Event %d order %d on market %s\n',
        'CancelOrderEvent',
        event.getEventId(),
        event.getOrderId(),
        Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === event.getMarket())
      );
      break;
  }
  console.log(result);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateOrderServiceStreamOrderEvents;
