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
import io.grpc.stub.StreamObserver;
import nl.tulipsolutions.api.common.Market;
import nl.tulipsolutions.api.common.Side;
import nl.tulipsolutions.api.priv.CreateOrderRequest;
import nl.tulipsolutions.api.priv.CreateOrderResponse;
import nl.tulipsolutions.api.priv.LimitOrderRequest;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc.PrivateOrderServiceStub;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PrivateOrderServiceCreateOrder {
    public static void run(ManagedChannel channel) {
        PrivateOrderServiceStub stub = PrivateOrderServiceGrpc.newStub(channel);

        // Create a request for a new order with an orderId that is the nanos since unix epoch
        Long orderId = Instant.now().toEpochMilli() * 1000000;
        CreateOrderRequest request = CreateOrderRequest.newBuilder()
            .setTonce(orderId)
            .setMarket(Market.BTC_EUR)
            .setLimitOrder(
                LimitOrderRequest.newBuilder()
                    .setSide(Side.BUY)
                    .setBaseAmount(1.0)
                    .setPrice(3000.0)
            )
            .build();

        // Make the request asynchronously with a 1s deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .createOrder(request, new StreamObserver<CreateOrderResponse>() {
                public void onNext(CreateOrderResponse value) {
                    System.out.println(value);
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateOrderService.CreateOrder error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateOrderService.CreateOrder completed");
                }
            });
    }
}
