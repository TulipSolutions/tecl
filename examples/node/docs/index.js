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

var grpc = require("grpc");

var auth = require("@tulipsolutions/tecl/auth");

var privateActiveOrdersServiceGetActiveOrders = require("./privateActiveOrdersServiceGetActiveOrders");
var privateActiveOrdersServiceStreamActiveOrders = require("./privateActiveOrdersServiceStreamActiveOrders");
var privateOrderServiceCancelOrder = require("./privateOrderServiceCancelOrder");
var privateOrderServiceCreateOrder = require("./privateOrderServiceCreateOrder");
var privateTradeServiceGetTrades = require("./privateTradeServiceGetTrades");
var privateTradeServiceStreamTrades = require("./privateTradeServiceStreamTrades");
var privateWalletServiceGetBalance = require("./privateWalletServiceGetBalance");
var privateWalletServiceStreamBalance = require("./privateWalletServiceStreamBalance");
var publicMarketDetailServiceGetMarketDetails = require("./publicMarketDetailServiceGetMarketDetails");
var publicMarketDetailServiceStreamMarketDetails = require("./publicMarketDetailServiceStreamMarketDetails");
var publicOrderbookServiceGetOrderbook = require("./publicOrderbookServiceGetOrderbook");
var publicOrderbookServiceStreamOrderbook = require("./publicOrderbookServiceStreamOrderbook");
var publicTickerServiceGetTickers = require("./publicTickerServiceGetTickers");
var publicTickerServiceStreamTickers = require("./publicTickerServiceStreamTickers");
var publicTradeServiceGetTrades = require("./publicTradeServiceGetTrades");
var publicTradeServiceStreamTrades = require("./publicTradeServiceStreamTrades");
var publicOhlcServiceGetOhlcData = require("./publicOhlcServiceGetOhlcData");
var publicOhlcServiceStreamOhlcData = require("./publicOhlcServiceStreamOhlcData");

// Create a secret byte array from the base64 decoded 'secret' string
var secret = Buffer.from("secret==", "base64");
var dummyJwt = "eyJraWQiOiI2YzY4OTIzMi03YTcxLTQ3NGItYjBlMi1lMmI1MzMyNDQzOWUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0In0.IL9QJQl55qn3oPsT7sFa7iwd5g1GsEQVr0IO7gCe1UmQdjT7jCIc-pUfjyYUgptPR8HBQl5ncXuBnxwjdXqOMwW1WhPmi_B3BRHQh3Sfu0zNXqKhkuz2-6DffXK1ek3DmK1NpaSikXtg2ruSQ4Uk5xHcnxmXY_SwEij0yot_JRKYEs-0RbyD5Z4jOFKcsbEW46WQmiWdgG3PUKiJT5TfdFd55JM55BwzSOdPIP1S_3dQ4VTDo30mWqAs1KaVbcPqCQmjT1PL0QScTp4w8-YPDcajcafIj98ve9LUoLBLraCIAX34D-hOxu643h9DoG2kIPFfZyXbkDTiUKOl7t-Ykg";

var host = "mockgrpc.test.tulipsolutions.nl:443";
var credentials = grpc.credentials.createSsl();

var options = {
  interceptors: [
    // Add an interceptor that signs messages with the provided secret.
    // Only messages to the private API that have a 'signed' field will be signed.
    auth.createMessageAuthInterceptor(secret),
    // Add an interceptor that adds a JWT token when a request to a private service is made.
    auth.createJwtInterceptor(dummyJwt)
  ]
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
