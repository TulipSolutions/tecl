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
var orderbook_pb = require("@tulipsolutions/tecl/pub/orderbook_pb");
var orderbook_grpc = require("@tulipsolutions/tecl/pub/orderbook_grpc_pb");

function publicOrderbookServiceStreamOrderbook(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example
  var client = new orderbook_grpc.PublicOrderbookServiceClient(host, credentials);

  // Create a request for streaming the BTC_EUR orderbook, with the greatest precision, largest length,
  // and highest update frequency
  // See TBD for semantics of Precision, Length and Frequency
  var request = new orderbook_pb.StreamOrderbookRequest();
  request.setMarket(orders_pb.Market.BTC_EUR);
  request.setPrecision(orderbook_pb.Precision.P3);
  request.setLength(orderbook_pb.Length.NUM_ENTRIES_100);
  request.setFrequency(orderbook_pb.Frequency.BEST_EFFORT);

  // Make the request asynchronously
  var call = client.streamOrderbook(request);
  call.on("data", function (value) {
    console.log(value.toObject())
  });
  call.on("error", function (err) {
    console.error("PublicOrderbookService.StreamOrderbook error: " + err.message)
  });
  call.on("end", function () {
    console.log("PublicOrderbookService.StreamOrderbook completed");
  });
  // CODEINCLUDE-END-MARKER: ref-code-example
}

module.exports = publicOrderbookServiceStreamOrderbook;
