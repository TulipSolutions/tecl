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
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import nl.tulipsolutions.api.auth.JwtClientInterceptor;
import nl.tulipsolutions.api.auth.MessageAuthClientInterceptor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// Provides code snippets for the examples in the reference documentation
class DocsMain {
    private final ManagedChannel channel;

    private static SslContext createSslContext(InputStream trustCertCollection) throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        if (trustCertCollection != null) {
            builder.trustManager(trustCertCollection);
        }
        return builder.build();
    }

    private void run() {
        PrivateActiveOrdersServiceGetActiveOrders.run(channel);
        PrivateActiveOrdersServiceStreamActiveOrders.run(channel);
        PrivateOrderServiceCreateOrder.run(channel);
        PrivateOrderServiceCancelOrder.run(channel);
        PrivateOrderServiceGetEventsForOrder.run(channel);
        PrivateOrderServiceGetOrderEvents.run(channel);
        PrivateOrderServiceStreamOrderEvents.run(channel);
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

    private DocsMain(
        String host,
        int port,
        SslContext sslContext
    ) throws NoSuchAlgorithmException, InvalidKeyException
    {
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

        channel = NettyChannelBuilder.forAddress(host, port)
            .sslContext(sslContext)
            .intercept(messageAuthClientInterceptor, jwtClientInterceptor)
            .build();
    }

    public static void main(String[] args) throws
        NoSuchAlgorithmException,
        InvalidKeyException,
        SSLException,
        FileNotFoundException
    {

        DocsMain main;
        switch (args.length) {
            case 0: {
                // Use system CA trust store to connect to public MockGrpc service.
                main = new DocsMain("mockgrpc.test.tulipsolutions.nl", 443, createSslContext(null));
                break;
            }
            case 2: {
                // Use Mock CA certificates from this repository
                // The server cert is set up to accept connections to localhost
                Path caCertPath = Paths.get("mockgrpc", "src", "main", "resources", "certs", "mock_ca.crt");
                if (!Files.exists(caCertPath)) {
                    // If this file is run from the examples workspace, the cert file will be placed here by Bazel
                    caCertPath = Paths.get("external", "nl_tulipsolutions_tecl", caCertPath.toString());
                }
                FileInputStream trustCertCollection = new FileInputStream(caCertPath.toFile());
                main = new DocsMain(args[0], Integer.parseInt(args[1]), createSslContext(trustCertCollection));
                break;
            }
            case 3: {
                // Use command line provided CA certificate bundle
                FileInputStream trustCertCollection = new FileInputStream(args[2]);
                main = new DocsMain(args[0], Integer.parseInt(args[1]), createSslContext(trustCertCollection));
                break;
            }
            default:
                main = null;
                System.out.println("USAGE: DocsMain [host port [trustCertCollectionFilePath]]");
                System.exit(1);
                break;
        }

        main.run();
    }
}
