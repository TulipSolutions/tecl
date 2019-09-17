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

function publicMarketDetailServiceStreamMarketDetails(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new market_detail_grpc.PublicMarketDetailServiceClient(host, credentials);

  // Create a request for streaming the details for all markets
  const request = new market_detail_pb.StreamMarketDetailsRequest();

  // Make the request asynchronously
  const call = client.streamMarketDetails(request, options);
  call.on('data', response => {
    console.log(response.toObject());
    // CODEINCLUDE-END-MARKER: ref-code-example-request
    parseAndPrint(response);
    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  });
  call.on('error', err => {
    console.error('PublicMarketDetailService.StreamMarketDetails error: ' + err.message);
  });
  call.on('end', () => {
    console.log('PublicMarketDetailService.StreamMarketDetails completed');
  });
  // CODEINCLUDE-END-MARKER: ref-code-example-request
}

function parseAndPrint(response) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
  console.log(
    util.format(
      '%s %s %s base currency: %s, quote currency: %s price resolution: %f price digits: %d ' +
        'amount resolution: %f amount digits: %d ' +
        'minimum base order amount: %f maximum base order amount: %f, minimum quote order amount: %f ' +
        'maximum quote order amount: %f',
      'MarketDetail',
      Object.keys(orders_pb.Market).find(key => orders_pb.Market[key] === response.getMarket()),
      Object.keys(market_detail_pb.MarketStatus).find(
        key => market_detail_pb.MarketStatus[key] === response.getMarketStatus()
      ),
      Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === response.getBase()),
      Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === response.getQuote()),
      response.getPriceResolution(),
      response.getPriceResolutionDigits(),
      response.getAmountResolution(),
      response.getAmountResolutionDigits(),
      response.getMinimumBaseOrderAmount(),
      response.getMaximumBaseOrderAmount(),
      response.getMinimumQuoteOrderAmount(),
      response.getMaximumQuoteOrderAmount()
    )
  );
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = publicMarketDetailServiceStreamMarketDetails;
