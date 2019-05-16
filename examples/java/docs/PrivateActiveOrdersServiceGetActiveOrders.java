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
import nl.tulipsolutions.api.priv.GetActiveOrdersRequest;
import nl.tulipsolutions.api.priv.OrderbookSnapshot;
import nl.tulipsolutions.api.priv.PrivateActiveOrdersServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateActiveOrdersServiceGrpc.PrivateActiveOrdersServiceStub;

import java.util.concurrent.TimeUnit;

public class PrivateActiveOrdersServiceGetActiveOrders {
    public static void run(ManagedChannel channel) {
        PrivateActiveOrdersServiceStub stub = PrivateActiveOrdersServiceGrpc.newStub(channel);

        // Create a request for all your active orders
        // no fields are set as it does not have any
        GetActiveOrdersRequest request = GetActiveOrdersRequest.getDefaultInstance();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getActiveOrders(request, new StreamObserver<OrderbookSnapshot>() {
                public void onNext(OrderbookSnapshot value) {
                    System.out.println(value);
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateActiveOrdersService.GetActiveOrders error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateActiveOrdersService.GetActiveOrders completed");
                }
            });
    }
}
