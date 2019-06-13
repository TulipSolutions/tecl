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

package docs;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import nl.tulipsolutions.api.common.SearchDirection;
import nl.tulipsolutions.api.common.Market;
import nl.tulipsolutions.api.priv.CancelOrderEvent;
import nl.tulipsolutions.api.priv.CreateLimitOrderEvent;
import nl.tulipsolutions.api.priv.FillOrderEvent;
import nl.tulipsolutions.api.priv.GetEventsForOrderRequest;
import nl.tulipsolutions.api.priv.GetOrderEventsResponse;
import nl.tulipsolutions.api.priv.OrderEvent;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc.PrivateOrderServiceStub;

import java.util.concurrent.TimeUnit;

public class PrivateOrderServiceGetEventsForOrder {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateOrderServiceStub stub = PrivateOrderServiceGrpc.newStub(channel);

        // Create a request for all order events for the order with ID 42 on the BTC_EUR market
        GetEventsForOrderRequest request = GetEventsForOrderRequest.newBuilder()
            .setOrderId(42)
            .setMarket(Market.BTC_EUR)
            .setSearchDirection(SearchDirection.FORWARD)
            .build();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getEventsForOrder(request, new StreamObserver<GetOrderEventsResponse>() {
                public void onNext(GetOrderEventsResponse response) {
                    System.out.println(response);
                    // CODEINCLUDE-END-MARKER: ref-code-example-request
                    parseAndPrint(response);
                    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateOrderService.GetEventsForOrder error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateOrderService.GetEventsForOrder completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(GetOrderEventsResponse response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        StringBuilder result = new StringBuilder(String.format("%s\n", response.getClass().getSimpleName()));
        for (OrderEvent event : response.getEventsList()) {
            String formattedEvent;
            switch (event.getEventCase()) {
                case CREATE_LIMIT_ORDER_EVENT:
                    CreateLimitOrderEvent limitOrderEvent = event.getCreateLimitOrderEvent();
                    formattedEvent = String.format(
                        "\t%s: Event %d order %d on market %s %s %f@%f\n",
                        limitOrderEvent.getClass().getSimpleName(),
                        event.getEventId(),
                        event.getOrderId(),
                        event.getMarket(),
                        limitOrderEvent.getSide(),
                        limitOrderEvent.getBaseAmount(),
                        limitOrderEvent.getPrice()
                    );
                    break;
                case FILL_ORDER_EVENT:
                    FillOrderEvent fillOrderEvent = event.getFillOrderEvent();
                    formattedEvent = String.format(
                        "\t%s: Event %d order %d on market %s %s %f@%f\n",
                        fillOrderEvent.getClass().getSimpleName(),
                        event.getEventId(),
                        event.getOrderId(),
                        event.getMarket(),
                        fillOrderEvent.getSide(),
                        fillOrderEvent.getBaseAmount(),
                        fillOrderEvent.getPrice()
                    );
                    break;
                case CANCEL_ORDER_EVENT:
                    CancelOrderEvent cancelOrderEvent = event.getCancelOrderEvent();
                    formattedEvent = String.format(
                        "\t%s: Event %d order %d on market %s\n",
                        cancelOrderEvent.getClass().getSimpleName(),
                        event.getEventId(),
                        event.getOrderId(),
                        event.getMarket()
                    );
                    break;
                default:
                    formattedEvent = "";
                    break;
            }

            result.append(formattedEvent);
        }
        System.out.println(result);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
