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

var ticker_pb = require("@tulipsolutions/tecl/pub/ticker_pb");
var ticker_grpc = require("@tulipsolutions/tecl/pub/ticker_grpc_pb");

function publicTickerServiceStreamTickers(host, credentials, options) {
  var client = new ticker_grpc.PublicTickerServiceClient(host, credentials);

  // Create a request for streaming the tickers for all markets
  var request = new ticker_pb.StreamTickersRequest();

  // Make the request asynchronously
  var call = client.streamTickers(request);
  call.on("data", function (value) {
    console.log(value.toObject())
  });
  call.on("error", function (err) {
    console.error("PublicTickerService.StreamTickers error: " + err.message)
  });
  call.on("end", function () {
    console.log("PublicTickerService.StreamTickers completed");
  });
}

module.exports = publicTickerServiceStreamTickers;
