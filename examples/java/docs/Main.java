// Copyright 2019 Tulip Solutions B.V.
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

package docs;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import nl.tulipsolutions.api.auth.JwtClientInterceptor;
import nl.tulipsolutions.api.auth.MessageAuthClientInterceptor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Provides code snippets for the examples in the reference documentation
class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // Create a SHA256 HMAC with the base64 decoded 'secret' string as its key
        byte[] secret = Base64.getDecoder().decode("secret==");
        String HMAC_SHA256 = "HmacSHA256";
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret, HMAC_SHA256);
        sha256_HMAC.init(keySpec);

        // Create an interceptor that signs messages with the provided secret.
        // Only messages to the private API that have a 'signed' field will be signed.
        MessageAuthClientInterceptor messageAuthClientInterceptor = new MessageAuthClientInterceptor(sha256_HMAC);

        String dummyJwt =
            "eyJraWQiOiI2YzY4OTIzMi03YTcxLTQ3NGItYjBlMi1lMmI1MzMyNDQzOWUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0In0.IL9QJQl55qn3oPsT7sFa7iwd5g1GsEQVr0IO7gCe1UmQdjT7jCIc-pUfjyYUgptPR8HBQl5ncXuBnxwjdXqOMwW1WhPmi_B3BRHQh3Sfu0zNXqKhkuz2-6DffXK1ek3DmK1NpaSikXtg2ruSQ4Uk5xHcnxmXY_SwEij0yot_JRKYEs-0RbyD5Z4jOFKcsbEW46WQmiWdgG3PUKiJT5TfdFd55JM55BwzSOdPIP1S_3dQ4VTDo30mWqAs1KaVbcPqCQmjT1PL0QScTp4w8-YPDcajcafIj98ve9LUoLBLraCIAX34D-hOxu643h9DoG2kIPFfZyXbkDTiUKOl7t-Ykg";

        // Create an interceptor that adds a JWT token when a request to a private service is made.
        JwtClientInterceptor jwtClientInterceptor = new JwtClientInterceptor(dummyJwt);

        // Set up a connection to the server.
        ManagedChannel channel = ManagedChannelBuilder.forAddress("mockgrpc.test.tulipsolutions.nl", 443)
            .intercept(messageAuthClientInterceptor, jwtClientInterceptor)
            .build();

        PrivateActiveOrdersServiceGetActiveOrders.run(channel);
        PrivateActiveOrdersServiceStreamActiveOrders.run(channel);
        PrivateOrderServiceCreateOrder.run(channel);
        PrivateOrderServiceCancelOrder.run(channel);
        PrivateTradeServiceGetTrades.run(channel);
        PrivateTradeServiceStreamTrades.run(channel);
        PrivateWalletServiceGetBalance.run(channel);
        PrivateWalletServiceStreamBalance.run(channel);

        PublicMarketDetailServiceGetMarketDetails.run(channel);
        PublicMarketDetailServiceStreamMarketDetails.run(channel);
        PublicOrderbookServiceGetOrderbook.run(channel);
        PublicOrderbookServiceStreamOrderbook.run(channel);
        PublicTickerServiceGetTickers.run(channel);
        PublicTickerServiceStreamTickers.run(channel);
        PublicTradeServiceGetTrades.run(channel);
        PublicTradeServiceStreamTrades.run(channel);
        PublicOhlcServiceGetOhlcData.run(channel);
        PublicOhlcServiceStreamOhlcData.run(channel);

        // Wait until cancel
        try {
            System.in.read();
        } catch (IOException ignored) {
        }
    }
}
