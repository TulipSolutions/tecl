// Copyright 2019 Tulipsolutions B.V.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Copied from https://github.com/grpc/grpc-java/blob/5dbe53c050d8e5f82b65df1e0c110e5c4fdaa5e7/examples/src/main/java/io/grpc/examples/authentication/JwtClientInterceptor.java
// Apache License, Version 2.0

package nl.tulipsolutions.api.auth;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import nl.tulipsolutions.api.Constant;

// Add a JWT token to outgoing calls to services in the private API
public class JwtClientInterceptor implements ClientInterceptor {

    private final String tokenValue;

    public JwtClientInterceptor(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> methodDescriptor,
        CallOptions callOptions,
        Channel channel
    )
    {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            channel.newCall(methodDescriptor, callOptions))
        {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (methodDescriptor.getFullMethodName().contains(".priv")) {
                    headers.put(Constant.JWT_METADATA_KEY, "Bearer " + tokenValue);
                }
                super.start(responseListener, headers);
            }
        };
    }
}
