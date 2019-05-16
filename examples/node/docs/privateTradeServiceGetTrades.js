/**
 * Copyright 2019 Tulipsolutions B.V.
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

var trade_pb = require("@tulipsolutions/tecl/priv/trade_pb");
var orders_pb = require("@tulipsolutions/tecl/common/orders_pb");
var private_trade_grpc = require("@tulipsolutions/tecl/priv/trade_grpc_pb");

function privateTradeServiceGetTrades(host, credentials, options) {
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
    }
  });
}

module.exports = privateTradeServiceGetTrades;
