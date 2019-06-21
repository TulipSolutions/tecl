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
const wallet_pb = require('@tulipsolutions/tecl/priv/wallet_pb');
const wallet_grpc = require('@tulipsolutions/tecl/priv/wallet_grpc_pb');

function privateWalletServiceStreamBalance(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new wallet_grpc.PrivateWalletServiceClient(host, credentials);

  // Create a request for streaming your balances for all currencies
  const request = new wallet_pb.StreamBalanceRequest();

  // Make the request asynchronously
  const call = client.streamBalance(request, options);
  call.on('data', response => {
    console.log(response.toObject());
    // CODEINCLUDE-END-MARKER: ref-code-example-request
    parseAndPrint(response);
    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  });
  call.on('error', err => {
    console.error('PrivateWalletService.StreamBalance error: ' + err.message);
  });
  call.on('end', () => {
    console.log('PrivateWalletService.StreamBalance completed');
  });
  // CODEINCLUDE-END-MARKER: ref-code-example-request
}

function parseAndPrint(response) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
  console.log(
    util.format(
      '%s %s total: %f locked: %f',
      'BalanceResponse',
      Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === response.getCurrency()),
      response.getTotalAmount(),
      response.getLockedAmount()
    )
  );
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateWalletServiceStreamBalance;
