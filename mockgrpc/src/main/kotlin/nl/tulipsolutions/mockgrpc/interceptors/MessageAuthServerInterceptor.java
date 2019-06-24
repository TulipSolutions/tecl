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

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.pgv.ReflectiveValidatorIndex;
import io.envoyproxy.pgv.ValidationException;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import nl.tulipsolutions.api.priv.Signed;
import nl.tulipsolutions.api.priv.SignedValidator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class MessageAuthServerInterceptor implements ServerInterceptor {
    private final SecretKeySpec key;

    public MessageAuthServerInterceptor(SecretKeySpec key) {
        this.key = key;
    }

    private SignedValidator signedValidator = new SignedValidator();
    private ReflectiveValidatorIndex validatorIndex = new ReflectiveValidatorIndex();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next
    )
    {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(
            call,
            headers
        ))
        {
            private boolean aborted = false;

            @Override
            public void onHalfClose() {
                if (!aborted) {
                    super.onHalfClose();
                }
            }

            @Override
            public void onMessage(ReqT message) {
                if (message instanceof GeneratedMessageV3) {

                    GeneratedMessageV3 protoMessage = (GeneratedMessageV3) message;

                    // Only handle messages with a field named "signed"
                    Descriptors.FieldDescriptor descriptor = protoMessage.getDescriptorForType()
                        .findFieldByName("signed");
                    if (descriptor != null) {
                        Signed signed = (Signed) protoMessage.getField(descriptor);
                        try {
                            // On each authenticated request initialize a new key to avoid having to lock a shared key
                            Mac mac = Mac.getInstance("HmacSHA256");
                            mac.init(key);

                            signedValidator.assertValid(signed, validatorIndex);
                            byte[] calculatedSig = mac.doFinal(signed.getRaw().toByteArray());
                            if (Arrays.equals(calculatedSig, signed.getSig().toByteArray())) {
                                @SuppressWarnings("unchecked")
                                ReqT deserializedMessage =
                                    (ReqT) protoMessage.getParserForType().parseFrom(signed.getRaw());
                                super.onMessage(deserializedMessage);
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (Byte b : calculatedSig) {
                                    sb.append(String.format("%02X", Byte.toUnsignedInt(b)).toLowerCase());
                                }

                                Status status = Status.INVALID_ARGUMENT.withDescription(
                                    "Invalid signature: " + Base64.getEncoder()
                                        .encodeToString(signed.getSig().toByteArray())
                                        + " should be (base64): " + Base64.getEncoder().encodeToString(calculatedSig)
                                        + " (hex): " + sb.toString()
                                );

                                aborted = true;
                                call.close(status, new Metadata());
                            }
                        } catch (ValidationException e) {
                            Status status =
                                Status.INVALID_ARGUMENT.withDescription("Message is not correctly signed, " + e.getLocalizedMessage());
                            aborted = true;
                            call.close(status, new Metadata());
                        } catch (InvalidProtocolBufferException e) {
                            Status status = Status.INVALID_ARGUMENT.withDescription(
                                "Unable to serialize bytes in signed.raw"
                            );
                            aborted = true;
                            call.close(status, new Metadata());
                        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                            e.printStackTrace();
                            aborted = true;
                            call.close(Status.INTERNAL, new Metadata());
                        }
                    } else {
                        super.onMessage(message);
                    }
                } else {
                    // The received value isn't a Protobuf message
                    // this should never happen since its already successfully parsed
                    aborted = true;
                    call.close(Status.INTERNAL, new Metadata());
                }
            }
        };
    }
}
