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

function publicOhlcServiceGetOhlcData(host, credentials, options) {
  var client = new ohlc_grpc.PublicOhlcServiceClient(host, credentials);

  // Create a request for the OhlcBins for specified market and intervals.
  var request = new ohlc_pb.GetOhlcRequest();
  request.setMarket(orders_pb.Market.BTC_EUR);
  request.setIntervalsList([ohlc_pb.Interval.ONE_SECOND, ohlc_pb.Interval.ONE_MINUTE, ohlc_pb.Interval.FIVE_MINUTES]);

  // Add a 1s deadline, and make the request asynchronously
  var deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  var callOptions = Object.assign({deadline: deadline}, options);
  client.getOhlcData(request, callOptions, function (err, response) {
    if (err) {
      console.error("PublicOhlcService.GetOhlcData error: " + err.message);
    }
    if (response) {
      console.log(response.toObject());
    }
  });
}

module.exports = publicOhlcServiceGetOhlcData;
