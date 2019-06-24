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


import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.Status;
import io.envoyproxy.pgv.ValidationException;
import io.envoyproxy.pgv.ValidatorIndex;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.StatusProto;
import nl.tulipsolutions.api.common.ValidateViolation;

import java.util.Collections;
import java.util.logging.Logger;

public class ValidatingServerInterceptor implements ServerInterceptor {
    private static Logger LOGGER = Logger.getLogger(ValidatingServerInterceptor.class.getCanonicalName());
    private final ValidatorIndex validatorIndex;

    public ValidatingServerInterceptor(ValidatorIndex validatorIndex) {
        this.validatorIndex = validatorIndex;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    )
    {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(
            call,
            headers
        ))
        {
            private boolean aborted = false;

            @Override
            public void onMessage(ReqT message) {
                try {
                    validatorIndex.validatorFor(message.getClass()).assertValid(message);
                    super.onMessage(message);
                } catch (ValidationException ex) {
                    if (ex.getReason().equals("Explicitly invalid")) {
                        LOGGER.severe(
                            "Unable to validate message because of missing validator for " +
                                message.getClass().getCanonicalName()
                        );
                        aborted = true;
                        call.close(io.grpc.Status.INTERNAL, new Metadata());
                        return;
                    }

                    ValidateViolation validateViolation = ValidateViolation.newBuilder()
                        .setField(ex.getField())
                        .setValue(ex.getValue().toString())
                        .setReason(ex.getReason())
                        .build();

                    // Note: this is a fairly complex way of generating a status with metadata but the required methods
                    // in io.grpc.protobuf.StatusProto are private and this way we guarantee compatibility with
                    // com.google.rpc.Status
                    Status status = Status.newBuilder()
                        .setCode(Code.INVALID_ARGUMENT.getNumber())
                        .setMessage(ex.getMessage())
                        .addAllDetails(Collections.singletonList(Any.pack(validateViolation)))
                        .build();
                    StatusRuntimeException statusRuntimeException = StatusProto.toStatusRuntimeException(status);

                    aborted = true;
                    call.close(statusRuntimeException.getStatus(), statusRuntimeException.getTrailers());
                }
            }

            @Override
            public void onHalfClose() {
                if (!aborted) {
                    super.onHalfClose();
                }
            }
        };
    }
}
