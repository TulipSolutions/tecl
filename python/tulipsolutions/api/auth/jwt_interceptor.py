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

import grpc
import collections

try:
    from python.tulipsolutions.api.auth import generic_client_interceptor
except ImportError:
    from tulipsolutions.api.auth import generic_client_interceptor


class _ClientCallDetails(
    collections.namedtuple(
        '_ClientCallDetails',
        ('method', 'timeout', 'metadata', 'credentials')
    ),
    grpc.ClientCallDetails
):
    pass


# Create an interceptor that adds a JWT token to outgoing calls to services in the private API
def create(jwt):
    def intercept_call(client_call_details, request_iterator, request_streaming, response_streaming):
        metadata = []
        if client_call_details.metadata is not None:
            metadata = list(client_call_details.metadata)

        if "priv" in client_call_details.method:
            metadata.append(("authorization", "Bearer " + jwt))

        client_call_details = _ClientCallDetails(
            client_call_details.method,
            client_call_details.timeout,
            metadata,
            client_call_details.credentials,
        )

        return client_call_details, request_iterator, None

    return generic_client_interceptor.create(intercept_call)
