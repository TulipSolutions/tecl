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

var wallet_pb = require("@tulipsolutions/tecl/priv/wallet_pb");
var wallet_grpc = require("@tulipsolutions/tecl/priv/wallet_grpc_pb");

function privateWalletServiceStreamBalance(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  var client = new wallet_grpc.PrivateWalletServiceClient(host, credentials);

  // Create a request for streaming your balances for all currencies
  var request = new wallet_pb.StreamBalanceRequest();

  // Make the request asynchronously
  var call = client.streamBalance(request, options);
  call.on("data", function (value) {
    console.log(value.toObject())
  });
  call.on("error", function (err) {
    console.error("PrivateWalletService.StreamBalance error: " + err.message)
  });
  call.on("end", function () {
    console.log("PrivateWalletService.StreamBalance completed");
  });
  // CODEINCLUDE-END-MARKER: ref-code-example-request
}

module.exports = privateWalletServiceStreamBalance;
