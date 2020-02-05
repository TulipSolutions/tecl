# Copyright 2019 Tulip Solutions B.V.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import hmac
import hashlib

try:
    from python.tulipsolutions.api.priv import message_authentication_pb2
except ImportError:
    from tulipsolutions.api.priv import message_authentication_pb2
try:
    from python.tulipsolutions.api.auth import generic_client_interceptor
except ImportError:
    from tulipsolutions.api.auth import generic_client_interceptor


def _create_signer(secret):
    def sign_request(request):
        # Same could be achieved with request.HasField("signed")
        # but that throws a ValueError if the "signed" field is not present
        if hasattr(request, "signed"):
            raw = request.SerializeToString()
            mac = hmac.new(secret, msg=raw, digestmod=hashlib.sha256)
            sig = mac.digest()
            request_type = type(request)
            signed_request = request_type(
                signed=message_authentication_pb2.Signed(
                    raw=raw,
                    sig=sig,
                )
            )
            request = signed_request
        return request

    return sign_request


# Create an interceptor that adds a signature to outgoing messages with a 'signed' field
def create(secret):
    signer = _create_signer(secret)

    def intercept_call(client_call_details, request_iterator, request_streaming, response_streaming):
        return client_call_details, iter(map(signer, request_iterator)), None

    return generic_client_interceptor.create(intercept_call)
