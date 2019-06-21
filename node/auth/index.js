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

const grpc = require('grpc');
const crypto = require('crypto');
const common_message_auth_proto = require('../priv/message_authentication_pb');

// Create an interceptor that adds a JWT token to outgoing calls to services in the private API
// Adding a JWT token could also be done using grpc.credentials.createFromMetadataGenerator
// However that does not allow for insecure connections (even to localhost)
function createJwtInterceptor(jwt) {
  return function jwtInterceptor(options, nextCall) {
    return new grpc.InterceptingCall(nextCall(options), {
      start: function(metadata, listener, next) {
        metadata.add('authorization', 'Bearer ' + jwt);
        next(metadata, listener);
      },
    });
  };
}

// Create an interceptor that adds a signature to outgoing messages with a 'signed' field
function createMessageAuthInterceptor(secret) {
  return function messageAuthInterceptor(options, nextCall) {
    return new grpc.InterceptingCall(nextCall(options), {
      sendMessage: function(message, next) {
        if (typeof message.getSigned === 'function') {
          // Mac newly instantiated per call as it does not have a reset
          const mac = crypto.createHmac('sha256', secret);
          const raw = message.serializeBinary();
          mac.update(raw);
          const sig = mac.digest();

          const signed = new common_message_auth_proto.Signed();
          signed.setRaw(raw);
          signed.setSig(sig);
          const signedMessage = new message.constructor();
          signedMessage.setSigned(signed);

          next(signedMessage);
        } else {
          next(message);
        }
      },
    });
  };
}

module.exports.createJwtInterceptor = createJwtInterceptor;
module.exports.createMessageAuthInterceptor = createMessageAuthInterceptor;
