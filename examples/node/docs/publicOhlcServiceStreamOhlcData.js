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

var ohlc_pb = require("@tulipsolutions/tecl/pub/ohlc_pb");
var ohlc_grpc = require("@tulipsolutions/tecl/pub/ohlc_grpc_pb");
var orders_pb = require("@tulipsolutions/tecl/common/orders_pb");

function publicOhlcServiceStreamOhlcData(host, credentials, options) {
  var client = new ohlc_grpc.PublicOhlcServiceClient(host, credentials);

  // Create a request for the OhlcBins for specified market and intervals.
  var request = new ohlc_pb.StreamOhlcRequest();
  request.setMarket(orders_pb.Market.BTC_EUR);
  request.setIntervalsList([ohlc_pb.Interval.ONE_SECOND, ohlc_pb.Interval.ONE_MINUTE, ohlc_pb.Interval.FIVE_MINUTES]);

  // Make the request asynchronously
  var call = client.streamOhlcData(request);
  call.on("data", function (value) {
    console.log(value.toObject());
  });
  call.on("error", function (err) {
    console.error("PublicOhlcService.StreamOhlcData error: " + err.message);
  });
  call.on("end", function () {
    console.log("PublicOhlcService.StreamOhlcData completed");
  });
}

module.exports = publicOhlcServiceStreamOhlcData;
