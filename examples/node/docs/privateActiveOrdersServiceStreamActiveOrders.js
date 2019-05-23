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

var order_pb = require("@tulipsolutions/tecl/priv/order_pb");
var order_grpc = require("@tulipsolutions/tecl/priv/order_grpc_pb");

function privateActiveOrdersServiceStreamActiveOrders(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example
  var client = new order_grpc.PrivateActiveOrdersServiceClient(host, credentials);

  // Create a request for streaming all your active orders
  // no fields are set as it does not have any
  var request = new order_pb.StreamActiveOrdersRequest();

  // Make the request asynchronously
  var call = client.streamActiveOrders(request, options);
  call.on("data", function (value) {
    console.log(value.toObject())
  });
  call.on("error", function (err) {
    console.error("PrivateActiveOrdersService.StreamActiveOrders error: " + err.message)
  });
  call.on("end", function () {
    console.log("PrivateActiveOrdersService.StreamActiveOrders completed");
  });
  // CODEINCLUDE-END-MARKER: ref-code-example
}

module.exports = privateActiveOrdersServiceStreamActiveOrders;
