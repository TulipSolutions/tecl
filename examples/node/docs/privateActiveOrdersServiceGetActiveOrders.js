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

var order_pb = require("@tulipsolutions/tecl/priv/order_pb");
var order_grpc = require("@tulipsolutions/tecl/priv/order_grpc_pb");

function privateActiveOrdersServiceGetActiveOrders(host, credentials, options) {
  var client = new order_grpc.PrivateActiveOrdersServiceClient(host, credentials);

  // Create a request for all your active orders
  // no fields are set as it does not have any
  var request = new order_pb.GetActiveOrdersRequest();

  // Add a 1s deadline, and make the request asynchronously
  var deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  var callOptions = Object.assign({deadline: deadline}, options);
  client.getActiveOrders(request, callOptions, function (err, response) {
    if (err) {
      console.error("PrivateActiveOrdersService.GetActiveOrders error: " + err.message);
    }
    if (response) {
      console.log(response.toObject());
    }
  });
}

module.exports = privateActiveOrdersServiceGetActiveOrders;
