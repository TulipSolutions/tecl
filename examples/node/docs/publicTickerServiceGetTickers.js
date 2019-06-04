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

var orders_pb = require("@tulipsolutions/tecl/common/orders_pb");
var ticker_pb = require("@tulipsolutions/tecl/pub/ticker_pb");
var ticker_grpc = require("@tulipsolutions/tecl/pub/ticker_grpc_pb");

function publicTickerServiceGetTickers(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  var client = new ticker_grpc.PublicTickerServiceClient(host, credentials);

  // Create a request for the tickers for all markets
  var request = new ticker_pb.GetTickersRequest();

  // Add a 1s deadline, and make the request asynchronously
  var deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  var callOptions = Object.assign({deadline: deadline}, options);
  client.getTickers(request, callOptions, function (err, response) {
    if (err) {
      console.error("PublicTickerService.GetTickers error: " + err.message);
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
  var resultString = util.format("%s\n", "Tickers");
  response.getTicksList().forEach(
    function (tick) {
      resultString +=
        util.format(
          "\t%s %s mid_price %f best_buy_price: %f best_buy_size: %f " +
          "best_sell_price: %f best_sell_size: %f open: %f, high: %f low: %f close: %f " +
          "volume_base: %f volume_quote: %f\n",
          "Tick",
          Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === tick.getMarket()),
          tick.getMidPrice(),
          tick.getBestBuyPrice(),
          tick.getBestBuySize(),
          tick.getBestSellPrice(),
          tick.getBestSellSize(),
          tick.getDailyOpen(),
          tick.getDailyHigh(),
          tick.getDailyLow(),
          tick.getDailyClose(),
          tick.getDailyVolumeBase(),
          tick.getDailyVolumeQuote()
        );
    }
  );
  console.log(resultString);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = publicTickerServiceGetTickers;
