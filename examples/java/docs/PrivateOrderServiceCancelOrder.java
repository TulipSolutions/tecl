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

package docs;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import nl.tulipsolutions.api.common.Market;
import nl.tulipsolutions.api.priv.CancelOrderRequest;
import nl.tulipsolutions.api.priv.CancelOrderResponse;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc.PrivateOrderServiceStub;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PrivateOrderServiceCancelOrder {
    public static void run(ManagedChannel channel) {
        PrivateOrderServiceStub stub = PrivateOrderServiceGrpc.newStub(channel);

        // Create a request for an order cancellation with a tonce that is the nanos since unix epoch
        Long tonce = Instant.now().toEpochMilli() * 1000000;
        CancelOrderRequest request = CancelOrderRequest.newBuilder()
            .setOrderId(1)
            .setTonce(tonce)
            .setMarket(Market.BTC_EUR)
            .build();

        // Make the request asynchronously with a 1s deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .cancelOrder(request, new StreamObserver<CancelOrderResponse>() {
                public void onNext(CancelOrderResponse value) {
                    System.out.println(value);
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateOrderService.CancelOrder error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateOrderService.CancelOrder completed");
                }
            });
    }
}
