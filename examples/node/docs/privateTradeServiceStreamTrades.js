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

var orders_pb = require("@tulipsolutions/tecl/common/orders_pb");
var trade_pb = require("@tulipsolutions/tecl/priv/trade_pb");
var private_trade_grpc = require("@tulipsolutions/tecl/priv/trade_grpc_pb");

function privateTradeServiceStreamTrades(host, credentials, options) {
  var client = new private_trade_grpc.PrivateTradeServiceClient(host, credentials);

  // Create a request for streaming all your trades in the BTC_EUR that occur after initiation of the request
  var request = new trade_pb.StreamPrivateTradesRequest();
  request.setMarket(orders_pb.Market.BTC_EUR);

  // Make the request asynchronously
  var call = client.streamTrades(request, options);
  call.on("data", function (value) {
    console.log(value.toObject())
  });
  call.on("error", function (err) {
    console.error("PrivateTradeService.StreamTrades error: " + err.message)
  });
  call.on("end", function () {
    console.log("PrivateTradeService.StreamTrades completed");
  });
}

module.exports = privateTradeServiceStreamTrades;
