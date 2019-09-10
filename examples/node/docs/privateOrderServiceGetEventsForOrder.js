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

function privateOrderServiceGetEventsForOrder(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new order_grpc_pb.PrivateOrderServiceClient(host, credentials);

  // Create a request for all order events for the order with ID 42 on the BTC_EUR market
  const request = new order_pb.GetEventsForOrderRequest();
  request.setOrderId('42');
  request.setMarket(orders_pb.Market.BTC_EUR);
  request.setSearchDirection(orders_pb.SearchDirection.FORWARD);

  // Add a 1s deadline, and make the request asynchronously
  const deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  const callOptions = Object.assign({ deadline: deadline }, options);
  client.getEventsForOrder(request, callOptions, (err, response) => {
    if (err) {
      console.error('PrivateOrderService.GetEventsForOrder error: ' + err.message);
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
  let result = util.format('%s\n', 'GetOrderEventsResponse');
  response.getEventsList().forEach(event => {
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
            result += util.format(
              '\t%s: Event %d order %d on market %s %s %f@%f %s\n',
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
            result += util.format(
              '\t%s: Event %d order %d on market %s %s %f %s\n',
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
          default: {
            throw 'Unknown order type';
          }
        }
        break;
      }
      case order_pb.OrderEvent.EventCase.FILL_ORDER_EVENT: {
        const fillOrderEvent = event.getFillOrderEvent();
        result += util.format(
          '\t%s: Event %d order %d on market %s %s %f@%f\n',
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
      case order_pb.OrderEvent.EventCase.CANCEL_ORDER_EVENT: {
        result += util.format(
          '\t%s: Event %d order %d on market %s\n',
          'CancelOrderEvent',
          event.getEventId(),
          event.getOrderId(),
          Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === event.getMarket())
        );
        break;
      }
      default: {
        throw 'Unknown OrderEvent type';
      }
    }
  });
  console.log(result);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateOrderServiceGetEventsForOrder;
