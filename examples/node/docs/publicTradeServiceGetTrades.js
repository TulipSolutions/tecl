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

const trade_pb = require('@tulipsolutions/tecl/pub/trade_pb');
const orders_pb = require('@tulipsolutions/tecl/common/orders_pb');
const public_trade_grpc = require('@tulipsolutions/tecl/pub/trade_grpc_pb');

function publicTradeServiceGetTrades(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new public_trade_grpc.PublicTradeServiceClient(host, credentials);

  // Create a request for the most recent trades in the BTC_EUR market
  const request = new trade_pb.GetPublicTradesRequest();
  request.setMarket(orders_pb.Market.BTC_EUR);

  // Add a 1s deadline, and make the request asynchronously
  const deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  const callOptions = Object.assign({ deadline: deadline }, options);
  client.getTrades(request, callOptions, (err, response) => {
    if (err) {
      console.error('PublicTradeService.GetTrades error: ' + err.message);
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
  let resultString = util.format('%s\n', 'PublicTrades');
  response.getTradesList().forEach(trade => {
    resultString += util.format(
      '\t%s: %s %s %f@%f quote_amount: %f time: %s id: %s\n',
      'PublicTrade',
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === trade.getMarket()),
      Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === trade.getSide()),
      trade.getBaseAmount(),
      trade.getPrice(),
      trade.getQuoteAmount(),
      trade.getTimestampNs(),
      trade.getEventId()
    );
  });
  console.log(resultString);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = publicTradeServiceGetTrades;
