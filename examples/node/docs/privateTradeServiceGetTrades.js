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
var util = require("util");

var trade_pb = require("@tulipsolutions/tecl/priv/trade_pb");
var orders_pb = require("@tulipsolutions/tecl/common/orders_pb");
var private_trade_grpc = require("@tulipsolutions/tecl/priv/trade_grpc_pb");

function privateTradeServiceGetTrades(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  var client = new private_trade_grpc.PrivateTradeServiceClient(host, credentials);

  // Create a request for your most recent trades in the BTC_EUR market
  var request = new trade_pb.GetPrivateTradesRequest();
  request.setMarket(orders_pb.Market.BTC_EUR);

  // Add a 1s deadline, and make the request asynchronously
  var deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  var callOptions = Object.assign({deadline: deadline}, options);
  client.getTrades(request, callOptions, function (err, response) {
    if (err) {
      console.error("PrivateTradeService.GetTrades error: " + err.message);
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
  var resultString = util.format("%s\n", "PrivateTrades");
  response.getTradesList().forEach(
    function (trade) {
      resultString +=
        util.format(
          "\t%s: %s %s %f@%f quote_amount: %f fee: %s %f time: %s id %s matched_orderid: %s\n",
          "PrivateTrade",
          Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === trade.getMarket()),
          Object.keys(orders_pb.Side).find(key => orders_pb.Side[key] === trade.getSide()),
          trade.getBaseAmount(),
          trade.getPrice(),
          trade.getQuoteAmount(),
          Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === trade.getFeeCurrency()),
          trade.getFee(),
          trade.getTimestampNs(),
          trade.getTradeId(),
          trade.getOrderId()
        );
    }
  );
  console.log(resultString);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateTradeServiceGetTrades;
