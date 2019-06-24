/*
 * Copyright 2019 Tulip Solutions B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nl.tulipsolutions.mockgrpc.interceptors;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import nl.tulipsolutions.api.Constant;

/**
 * Interceptor that verifies the received JWT is sent
 * - in the correct header key
 * - has the `Bearer ` prefix
 * - is equal to the expected Mock JWT
 */
public class JwtServerInterceptor implements ServerInterceptor {
    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
    };
    private final String mockJwt;

    public JwtServerInterceptor(String mockJwt) {
        this.mockJwt = mockJwt;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    )
    {
        // Calls to the public API do not need a JWT token
        if (call.getMethodDescriptor().getFullMethodName().startsWith("tulipsolutions.api.pub")) {
            return next.startCall(call, headers);
        }

        String authorization = headers.get(Constant.JWT_METADATA_KEY);
        if (authorization == null || !authorization.substring(0, 7).toLowerCase().equals("bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("JWT Token is missing from Metadata"), new Metadata());
            return NOOP_LISTENER;
        }
        String token = authorization.substring(7);

        if (mockJwt.equals(token)) {
            return next.startCall(call, headers);
        } else {
            call.close(Status.UNAUTHENTICATED.withDescription("Received invalid JWT token: " + token), new Metadata());
            return NOOP_LISTENER;
        }
    }
}
