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

function privateWalletServiceGetBalance(host, credentials, options) {
  // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
  const client = new wallet_grpc.PrivateWalletServiceClient(host, credentials);

  // Create a request for your balances for all currencies
  const request = new wallet_pb.GetBalanceRequest();

  // Add a 1s deadline, and make the request asynchronously
  const deadline = new Date().setSeconds(new Date().getSeconds() + 1);
  const callOptions = Object.assign({ deadline: deadline }, options);
  client.getBalance(request, callOptions, (err, response) => {
    if (err) {
      console.error('PrivateWalletService.GetBalance error: ' + err.message);
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
  let resultString = util.format('%s\n', 'BalanceSnapshot');
  response.getBalanceResponseList().forEach(balance => {
    resultString += util.format(
      '\t%s %s total: %f locked: %f\n',
      'BalanceResponse',
      Object.keys(orders_pb.Currency).find(key => orders_pb.Currency[key] === balance.getCurrency()),
      balance.getTotalAmount(),
      balance.getLockedAmount()
    );
  });
  console.log(resultString);
  // CODEINCLUDE-END-MARKER: ref-code-example-response
}

module.exports = privateWalletServiceGetBalance;
