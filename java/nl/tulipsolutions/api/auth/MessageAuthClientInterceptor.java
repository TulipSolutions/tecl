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

package nl.tulipsolutions.api.auth;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.MethodDescriptor;
import nl.tulipsolutions.api.priv.Signed;

import javax.crypto.Mac;

// Add a signature to outgoing messages with a 'signed' field
public class MessageAuthClientInterceptor implements ClientInterceptor {
    private final Mac hmac;

    public MessageAuthClientInterceptor(Mac sha256_HMAC) {
        this.hmac = sha256_HMAC;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions,
        Channel next
    )
    {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                if (message instanceof GeneratedMessageV3) {
                    GeneratedMessageV3 protoMessage = (GeneratedMessageV3) message;
                    Descriptors.FieldDescriptor descriptor = protoMessage.getDescriptorForType()
                        .findFieldByName("signed");

                    if (descriptor != null) {
                        ByteString serialized = protoMessage.toByteString();

                        byte[] sig;
                        // doFinal mutates internal state thus only one call can run at the same time
                        synchronized (hmac) {
                            sig = hmac.doFinal(serialized.toByteArray());
                        }

                        Signed signed = Signed.newBuilder()
                            .setRaw(serialized)
                            .setSig(ByteString.copyFrom(sig))
                            .build();

                        @SuppressWarnings("unchecked")
                        ReqT signedMessage = (ReqT) protoMessage.newBuilderForType()
                            .setField(descriptor, signed)
                            .build();
                        super.sendMessage(signedMessage);
                    } else {
                        super.sendMessage(message);
                    }
                } else {
                    // The message isn't a Protobuf message, this should never happen
                    super.halfClose();
                }
            }
        };
    }
}
