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

var market_detail_pb = require("@tulipsolutions/tecl/pub/market_detail_pb");
var market_detail_grpc = require("@tulipsolutions/tecl/pub/market_detail_grpc_pb");

function privateWalletServiceStreamBalance(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example
  var client = new market_detail_grpc.PublicMarketDetailServiceClient(host, credentials);

  // Create a request for streaming the details for all markets
  var request = new market_detail_pb.StreamMarketDetailsRequest();

  // Make the request asynchronously
  var call = client.streamMarketDetails(request, options);
  call.on("data", function (value) {
    console.log(value.toObject())
  });
  call.on("error", function (err) {
    console.error("PublicMarketDetailService.StreamMarketDetails error: " + err.message)
  });
  call.on("end", function () {
    console.log("PublicMarketDetailService.StreamMarketDetails completed");
  });
  // CODEINCLUDE-END-MARKER: ref-code-example
}

module.exports = privateWalletServiceStreamBalance;
