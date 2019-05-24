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
import nl.tulipsolutions.api.priv.ActiveOrderStatus;
import nl.tulipsolutions.api.priv.GetActiveOrdersRequest;
import nl.tulipsolutions.api.priv.LimitOrderStatus;
import nl.tulipsolutions.api.priv.OrderbookSnapshot;
import nl.tulipsolutions.api.priv.PrivateActiveOrdersServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateActiveOrdersServiceGrpc.PrivateActiveOrdersServiceStub;

import java.util.concurrent.TimeUnit;

public class PrivateActiveOrdersServiceGetActiveOrders {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateActiveOrdersServiceStub stub = PrivateActiveOrdersServiceGrpc.newStub(channel);

        // Create a request for all your active orders
        // no fields are set as it does not have any
        GetActiveOrdersRequest request = GetActiveOrdersRequest.getDefaultInstance();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getActiveOrders(request, new StreamObserver<OrderbookSnapshot>() {
                public void onNext(OrderbookSnapshot response) {
                    System.out.println(response);
                    // CODEINCLUDE-END-MARKER: ref-code-example-request
                    parseAndPrint(response);
                    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateActiveOrdersService.GetActiveOrders error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateActiveOrdersService.GetActiveOrders completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(OrderbookSnapshot response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        String result_string = String.format("%s\n", response.getClass().getSimpleName());
        for (ActiveOrderStatus order : response.getOrdersList()) {
            String order_type_detail = "";
            if (order.getOrderCase() == ActiveOrderStatus.OrderCase.LIMIT_ORDER) {
                LimitOrderStatus limitOrder = order.getLimitOrder();
                order_type_detail =
                    String.format(
                        "%s %f@%f remaining %f",
                        limitOrder.getSide().getValueDescriptor().getName(),
                        limitOrder.getBaseAmount(),
                        limitOrder.getPrice(),
                        limitOrder.getBaseRemaining()
                    );
            } else {
                order_type_detail = "removed from orderbook";
            }
            result_string +=
                String.format(
                    "\t%s: %d for market %s %s\n",
                    order.getClass().getSimpleName(),
                    order.getOrderId(),
                    order.getMarket().getValueDescriptor().getName(),
                    order_type_detail
                );
        }
        System.out.println(result_string);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
