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
const trade_pb = require('@tulipsolutions/tecl/priv/trade_pb');
const private_trade_grpc = require('@tulipsolutions/tecl/priv/trade_grpc_pb');

function privateTradeServiceStreamTrades(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new private_trade_grpc.PrivateTradeServiceClient(host, credentials);

  const start = Date.now() * 1000000;
  // Create a request for streaming all your trades in the BTC_EUR market that occur after start
  const request = new trade_pb.StreamPrivateTradesRequest();
  request.addMarkets(orders_pb.Market.BTC_EUR);
  request.setSearchDirection(orders_pb.SearchDirection.FORWARD);
  request.setTimestampNs(start.toString());

  // Make the request asynchronously
  const call = client.streamTrades(request, options);
  call.on('data', response => {
    console.log(response.toObject());
    // CODEINCLUDE-END-MARKER: ref-code-example-request
    parseAndPrint(response);
    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  });
  call.on('error', err => {
    console.error('PrivateTradeService.StreamTrades error: ' + err.message);
  });
  call.on('end', () => {
    console.log('PrivateTradeService.StreamTrades completed');
  });
  // CODEINCLUDE-END-MARKER: ref-code-example-request
}

function parseAndPrint(response) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
  console.log(
    util.format(
      '%s: %s %s %f@%f quote_amount: %f fee: %s %f time: %s id: %s matched_orderid: %s',
      'PrivateTrade',
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === response.getMarket()),
      Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === response.getSide()),
      response.getBaseAmount(),
      response.getPrice(),
      response.getQuoteAmount(),
      Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === response.getFeeCurrency()),
      response.getFee(),
      response.getTimestampNs(),
      response.getEventId(),
      response.getOrderId()
    )
  );
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateTradeServiceStreamTrades;
