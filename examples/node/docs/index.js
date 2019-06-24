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

const fs = require('fs');

const grpc = require('grpc');

const auth = require('@tulipsolutions/tecl/auth');

const privateActiveOrdersServiceGetActiveOrders = require('./privateActiveOrdersServiceGetActiveOrders');
const privateActiveOrdersServiceStreamActiveOrders = require('./privateActiveOrdersServiceStreamActiveOrders');
const privateOrderServiceCancelOrder = require('./privateOrderServiceCancelOrder');
const privateOrderServiceCreateOrder = require('./privateOrderServiceCreateOrder');
const privateTradeServiceGetTrades = require('./privateTradeServiceGetTrades');
const privateTradeServiceStreamTrades = require('./privateTradeServiceStreamTrades');
const privateWalletServiceGetBalance = require('./privateWalletServiceGetBalance');
const privateWalletServiceStreamBalance = require('./privateWalletServiceStreamBalance');
const publicMarketDetailServiceGetMarketDetails = require('./publicMarketDetailServiceGetMarketDetails');
const publicMarketDetailServiceStreamMarketDetails = require('./publicMarketDetailServiceStreamMarketDetails');
const publicOrderbookServiceGetOrderbook = require('./publicOrderbookServiceGetOrderbook');
const publicOrderbookServiceStreamOrderbook = require('./publicOrderbookServiceStreamOrderbook');
const publicTickerServiceGetTickers = require('./publicTickerServiceGetTickers');
const publicTickerServiceStreamTickers = require('./publicTickerServiceStreamTickers');
const publicTradeServiceGetTrades = require('./publicTradeServiceGetTrades');
const publicTradeServiceStreamTrades = require('./publicTradeServiceStreamTrades');
const publicOhlcServiceGetOhlcData = require('./publicOhlcServiceGetOhlcData');
const publicOhlcServiceStreamOhlcData = require('./publicOhlcServiceStreamOhlcData');

const args = process.argv;
let host;
let credentials;

switch (args.length) {
  case 2: {
    // Use system CA trust store to connect to public MockGrpc service.
    host = 'mockgrpc.test.tulipsolutions.nl:443';
    credentials = grpc.credentials.createSsl();
    break;
  }
  case 4: {
    // Use Mock CA certificates from this repository
    // The server cert is set up to accept connections to localhost
    let caCertPath = 'mockgrpc/src/main/resources/certs/mock_ca.crt';
    if (!fs.existsSync(caCertPath)) {
      // If this file is run from the examples workspace, the cert file will be placed here by Bazel
      caCertPath = 'external/nl_tulipsolutions_tecl/' + caCertPath;
    }
    const trustCertCollection = fs.readFileSync(caCertPath);
    credentials = grpc.credentials.createSsl(trustCertCollection);
    host = args[2] + ':' + args[3];
    break;
  }
  case 5: {
    // Use command line provided CA certificate bundle
    host = args[2] + ':' + args[3];
    const trustCertCollection = fs.readFileSync(args[4]);
    credentials = grpc.credentials.createSsl(trustCertCollection);
    break;
  }
  default: {
    console.log('USAGE: DocsMain [host port [trustCertCollectionFilePath]]');
    process.exit(1);
  }
}

// Create a secret byte array from the base64 decoded 'secret' string
const secret = Buffer.from('secret==', 'base64');
const dummyJwt =
  'eyJraWQiOiI2YzY4OTIzMi03YTcxLTQ3NGItYjBlMi1lMmI1MzMyNDQzOWUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0In0.IL9QJQl55qn3oPsT7sFa7iwd5g1GsEQVr0IO7gCe1UmQdjT7jCIc-pUfjyYUgptPR8HBQl5ncXuBnxwjdXqOMwW1WhPmi_B3BRHQh3Sfu0zNXqKhkuz2-6DffXK1ek3DmK1NpaSikXtg2ruSQ4Uk5xHcnxmXY_SwEij0yot_JRKYEs-0RbyD5Z4jOFKcsbEW46WQmiWdgG3PUKiJT5TfdFd55JM55BwzSOdPIP1S_3dQ4VTDo30mWqAs1KaVbcPqCQmjT1PL0QScTp4w8-YPDcajcafIj98ve9LUoLBLraCIAX34D-hOxu643h9DoG2kIPFfZyXbkDTiUKOl7t-Ykg';

const options = {
  interceptors: [
    // Add an interceptor that signs messages with the provided secret.
    // Only messages to the private API that have a 'signed' field will be signed.
    auth.createMessageAuthInterceptor(secret),
    // Add an interceptor that adds a JWT token when a request to a private service is made.
    auth.createJwtInterceptor(dummyJwt),
  ],
};

privateActiveOrdersServiceGetActiveOrders(host, credentials, options);
privateActiveOrdersServiceStreamActiveOrders(host, credentials, options);
privateOrderServiceCancelOrder(host, credentials, options);
privateOrderServiceCreateOrder(host, credentials, options);
privateTradeServiceGetTrades(host, credentials, options);
privateTradeServiceStreamTrades(host, credentials, options);
privateWalletServiceGetBalance(host, credentials, options);
privateWalletServiceStreamBalance(host, credentials, options);
publicMarketDetailServiceGetMarketDetails(host, credentials, options);
publicMarketDetailServiceStreamMarketDetails(host, credentials, options);
publicOrderbookServiceGetOrderbook(host, credentials, options);
publicOrderbookServiceStreamOrderbook(host, credentials, options);
publicTickerServiceGetTickers(host, credentials, options);
publicTickerServiceStreamTickers(host, credentials, options);
publicTradeServiceGetTrades(host, credentials, options);
publicTradeServiceStreamTrades(host, credentials, options);
publicOhlcServiceGetOhlcData(host, credentials, options);
publicOhlcServiceStreamOhlcData(host, credentials, options);
