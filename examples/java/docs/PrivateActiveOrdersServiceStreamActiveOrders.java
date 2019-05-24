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
import nl.tulipsolutions.api.priv.LimitOrderStatus;
import nl.tulipsolutions.api.priv.PrivateActiveOrdersServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateActiveOrdersServiceGrpc.PrivateActiveOrdersServiceStub;
import nl.tulipsolutions.api.priv.StreamActiveOrdersRequest;


public class PrivateActiveOrdersServiceStreamActiveOrders {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateActiveOrdersServiceStub stub = PrivateActiveOrdersServiceGrpc.newStub(channel);

        // Create a request for streaming all your active orders
        // no fields are set as it does not have any
        StreamActiveOrdersRequest request = StreamActiveOrdersRequest.getDefaultInstance();

        // Make the request asynchronously
        stub.streamActiveOrders(request, new StreamObserver<ActiveOrderStatus>() {
            public void onNext(ActiveOrderStatus response) {
                System.out.println(response);
                // CODEINCLUDE-END-MARKER: ref-code-example-request
                parseAndPrint(response);
                // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
            }

            public void onError(Throwable t) {
                System.err.println("PrivateActiveOrdersService.StreamActiveOrders error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PrivateActiveOrdersService.StreamActiveOrders completed");
            }
        });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(ActiveOrderStatus response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        String order_type_detail = "";
        if (response.getOrderCase() == ActiveOrderStatus.OrderCase.LIMIT_ORDER) {
            LimitOrderStatus limitOrder = response.getLimitOrder();
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
        System.out.println(
            String.format(
                "%s: %d for market %s %s",
                response.getClass().getSimpleName(),
                response.getOrderId(),
                response.getMarket().getValueDescriptor().getName(),
                order_type_detail
            )
        );
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
