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

var orders_pb = require("@tulipsolutions/tecl/common/orders_pb");
var orderbook_pb = require("@tulipsolutions/tecl/pub/orderbook_pb");
var orderbook_grpc = require("@tulipsolutions/tecl/pub/orderbook_grpc_pb");
var order_pb = require("@tulipsolutions/tecl/priv/order_pb");
var order_grpc = require("@tulipsolutions/tecl/priv/order_grpc_pb");
var grpc = require("grpc");
var auth = require("@tulipsolutions/tecl/auth");

// Subscribe to a public orderbook stream and set a new order

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

// Construct clients for accessing PublicOrderbookService and PrivateOrderService
var orderbookServiceClient = new orderbook_grpc.PublicOrderbookServiceClient(host, credentials);
var orderServiceClient = new order_grpc.PrivateOrderServiceClient(host, credentials);

// Create a request for the BTC_EUR orderbook, with the greatest precision, largest length,
// and highest update frequency
var streamOrderbookRequest = new orderbook_pb.StreamOrderbookRequest();
streamOrderbookRequest.setMarket(orders_pb.Market.BTC_EUR);
streamOrderbookRequest.setPrecision(orderbook_pb.Precision.P3);
streamOrderbookRequest.setLength(orderbook_pb.Length.NUM_ENTRIES_100);
streamOrderbookRequest.setFrequency(orderbook_pb.Frequency.BEST_EFFORT);

// make the request asynchronously and add callbacks for data and stream end
var streamOrderbookCall = orderbookServiceClient.streamOrderbook(streamOrderbookRequest);
streamOrderbookCall.on("data", function (value) {
  console.log(value.toObject())
});
streamOrderbookCall.on("error", function (err) {
  console.error("PublicOrderbookService.StreamOrderbook error: " + err.message)
});
streamOrderbookCall.on("end", function () {
  console.log("PublicOrderbookService.StreamOrderbook completed");
});

// Create a request for a new order with an orderId that is the nanos since unix epoch
var orderId = Date.now() * 1000000;
var limitOrderRequest = new order_pb.LimitOrderRequest();
limitOrderRequest.setSide(orders_pb.Side.BUY);
limitOrderRequest.setBaseAmount(1.0);
limitOrderRequest.setPrice(3000);
var createOrderRequest = new order_pb.CreateOrderRequest();
createOrderRequest.setMarket(orders_pb.Market.BTC_EUR);
createOrderRequest.setTonce(orderId);
createOrderRequest.setLimitOrder(limitOrderRequest);

// Add a 1s deadline, and make the request asynchronously
var deadline = new Date().setSeconds(new Date().getSeconds() + 1);
options["deadline"] = deadline;
orderServiceClient.createOrder(createOrderRequest, options, function (err, response) {
  if (err) {
    console.error("PrivateOrderService.CreateOrder error: " + err.message);
  }
  if (response) {
    console.log(response.toObject());
  }
});
