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

package hello_exchange;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import nl.tulipsolutions.api.auth.JwtClientInterceptor;
import nl.tulipsolutions.api.auth.MessageAuthClientInterceptor;
import nl.tulipsolutions.api.common.Market;
import nl.tulipsolutions.api.common.Side;
import nl.tulipsolutions.api.priv.CreateOrderRequest;
import nl.tulipsolutions.api.priv.CreateOrderResponse;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc.PrivateOrderServiceStub;
import nl.tulipsolutions.api.pub.Frequency;
import nl.tulipsolutions.api.pub.Length;
import nl.tulipsolutions.api.pub.OrderbookEntry;
import nl.tulipsolutions.api.pub.Precision;
import nl.tulipsolutions.api.pub.PublicOrderbookServiceGrpc;
import nl.tulipsolutions.api.pub.PublicOrderbookServiceGrpc.PublicOrderbookServiceStub;
import nl.tulipsolutions.api.pub.StreamOrderbookRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

// Subscribe to a public orderbook stream and set a new order
class HelloExchange {

    // CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-request
    private static void streamOrderbook(PublicOrderbookServiceStub orderbookServiceStub) {
        // Create a request for the BTC_EUR orderbook, with the greatest precision, largest length,
        // and highest update frequency.
        StreamOrderbookRequest streamOrderbookRequest = StreamOrderbookRequest.newBuilder()
            .setMarket(Market.BTC_EUR)
            .setPrecision(Precision.P3)
            .setLength(Length.NUM_ENTRIES_100)
            .setFrequency(Frequency.BEST_EFFORT)
            .build();

        // Create a callback for a stream orderbook request and make the request asynchronously
        StreamObserver<OrderbookEntry> streamOrderbookResponseObserver = new StreamObserver<OrderbookEntry>() {
            @Override
            public void onNext(OrderbookEntry response) {
                System.out.println(response);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("PublicOrderbookService.StreamOrderbook error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("PublicOrderbookService.StreamOrderbook completed");
            }
        };
        orderbookServiceStub.streamOrderbook(streamOrderbookRequest, streamOrderbookResponseObserver);
    }
    // CODEINCLUDE-END-MARKER: getting-started-orderbook-service-request

    // CODEINCLUDE-BEGIN-MARKER: getting-started-create-order-request
    private static void createOrder(PrivateOrderServiceStub orderServiceStub) {
        // Create a request for a new order with an orderId that is the nanos since unix epoch
        Long orderId = Instant.now().toEpochMilli() * 1000000;
        CreateOrderRequest.Builder createOrderRequest = CreateOrderRequest.newBuilder()
            .setTonce(orderId)
            .setMarket(Market.BTC_EUR);
        createOrderRequest.getLimitOrderBuilder()
            .setSide(Side.BUY)
            .setBaseAmount(1.0)
            .setPrice(3000.0);

        // Create a callback for a create order request
        StreamObserver<CreateOrderResponse> createOrderResponseObserver = new StreamObserver<CreateOrderResponse>() {
            @Override
            public void onNext(CreateOrderResponse response) {
                System.out.println(response);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("PrivateOrderService.CreateOrder error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("PrivateOrderService.CreateOrder completed");
            }
        };
        // Make the request asynchronously with a 1s deadline
        orderServiceStub
            .withDeadlineAfter(1, TimeUnit.SECONDS)
            .createOrder(createOrderRequest.build(), createOrderResponseObserver);
    }
    // CODEINCLUDE-END-MARKER: getting-started-create-order-request

    // CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
    // CODEINCLUDE-BEGIN-MARKER: getting-started-create-order-authentication
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {

        // CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
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

        // CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
        // Set up a connection to the server.
        ManagedChannel channel = ManagedChannelBuilder.forAddress("mockgrpc.test.tulipsolutions.nl", 443)
            .intercept(messageAuthClientInterceptor, jwtClientInterceptor)
            .build();

        // CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
        // Construct clients for accessing PublicOrderbookService and PrivateOrderService using the existing connection.
        // CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
        PublicOrderbookServiceStub orderbookServiceStub = PublicOrderbookServiceGrpc.newStub(channel);
        // CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
        PrivateOrderServiceStub orderServiceStub = PrivateOrderServiceGrpc.newStub(channel);

        // Stream the orderbook and create an order asynchronously
        // CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
        streamOrderbook(orderbookServiceStub);
        // CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
        createOrder(orderServiceStub);
        // CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init

        // Wait until cancel
        try {
            System.in.read();
        } catch (IOException ignored) {
        }
    }
    // CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
    // CODEINCLUDE-END-MARKER: getting-started-create-order-authentication
}
