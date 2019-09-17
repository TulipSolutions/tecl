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
const util = require('util');

const orders_pb = require('@tulipsolutions/tecl/common/orders_pb');
const market_detail_pb = require('@tulipsolutions/tecl/pub/market_detail_pb');
const market_detail_grpc = require('@tulipsolutions/tecl/pub/market_detail_grpc_pb');

function publicMarketDetailServiceGetMarketDetails(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new market_detail_grpc.PublicMarketDetailServiceClient(host, credentials);

  // Create a request for the details for all markets
  const request = new market_detail_pb.GetMarketDetailsRequest();

  // Add a 1s deadline, and make the request asynchronously
  const deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  const callOptions = Object.assign({ deadline: deadline }, options);
  client.getMarketDetails(request, callOptions, (err, response) => {
    if (err) {
      console.error('PublicMarketDetailService.GetMarketDetails error: ' + err.message);
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
  let resultString = util.format('%s\n', 'MarketDetails');
  response.getMarketDetailsList().forEach(detail => {
    resultString += util.format(
      '\t%s %s %s base currency: %s, quote currency: %s price resolution: %f price digits: %d ' +
        'amount resolution: %f amount digits: %d ' +
        'minimum base order amount: %f maximum base order amount: %f, minimum quote order amount: %f ' +
        'maximum quote order amount: %f\n',
      'MarketDetail',
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === detail.getMarket()),
      Object.keys(market_detail_pb.MarketStatus).find(
        key => market_detail_pb.MarketStatus[key] === detail.getMarketStatus()
      ),
      Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === detail.getBase()),
      Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === detail.getQuote()),
      detail.getPriceResolution(),
      detail.getPriceResolutionDigits(),
      detail.getAmountResolution(),
      detail.getAmountResolutionDigits(),
      detail.getMinimumBaseOrderAmount(),
      detail.getMaximumBaseOrderAmount(),
      detail.getMinimumQuoteOrderAmount(),
      detail.getMaximumQuoteOrderAmount()
    );
  });
  console.log(resultString);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = publicMarketDetailServiceGetMarketDetails;
