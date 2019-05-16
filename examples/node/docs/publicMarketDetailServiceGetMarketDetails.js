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

function publicMarketDetailServiceGetMarketDetails(host, credentials, options) {
  var client = new market_detail_grpc.PublicMarketDetailServiceClient(host, credentials);

  // Create a request for the details for all markets
  var request = new market_detail_pb.GetMarketDetailsRequest();

  // Add a 1s deadline, and make the request asynchronously
  var deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  var callOptions = Object.assign({deadline: deadline}, options);
  client.getMarketDetails(request, callOptions, function (err, response) {
    if (err) {
      console.error("PublicMarketDetailService.GetMarketDetails error: " + err.message);
    }
    if (response) {
      console.log(response.toObject());
    }
  });
}

module.exports = publicMarketDetailServiceGetMarketDetails;
