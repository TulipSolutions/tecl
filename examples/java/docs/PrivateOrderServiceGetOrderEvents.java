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
import nl.tulipsolutions.api.priv.GetOrderEventsRequest;
import nl.tulipsolutions.api.priv.GetOrderEventsResponse;
import nl.tulipsolutions.api.priv.OrderEvent;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc.PrivateOrderServiceStub;

import java.util.concurrent.TimeUnit;

public class PrivateOrderServiceGetOrderEvents {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateOrderServiceStub stub = PrivateOrderServiceGrpc.newStub(channel);

        // Create a request for 10 most recent order events on the BTC_EUR and BTC_USD markets
        GetOrderEventsRequest request = GetOrderEventsRequest.newBuilder()
            .addMarkets(Market.BTC_EUR)
            .addMarkets(Market.BTC_USD)
            .setSearchDirection(SearchDirection.BACKWARD)
            .setLimit(10)
            .build();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getOrderEvents(request, new StreamObserver<GetOrderEventsResponse>() {
                public void onNext(GetOrderEventsResponse response) {
                    System.out.println(response);
                    // CODEINCLUDE-END-MARKER: ref-code-example-request
                    parseAndPrint(response);
                    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateOrderService.GetOrderEvents error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateOrderService.GetOrderEvents completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(GetOrderEventsResponse response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        StringBuilder formattedResponse = new StringBuilder(String.format("%s\n", response.getClass().getSimpleName()));
        for (OrderEvent event : response.getEventsList()) {
            String formattedEvent;
            switch (event.getEventCase()) {
                case CREATE_LIMIT_ORDER_EVENT:
                    CreateLimitOrderEvent limitOrder = event.getCreateLimitOrderEvent();
                    formattedEvent = String.format(
                        "\t%s: Event %d order %d on market %s %s %f@%f\n",
                        limitOrder.getClass().getSimpleName(),
                        event.getEventId(),
                        event.getOrderId(),
                        event.getMarket(),
                        limitOrder.getSide(),
                        limitOrder.getBaseAmount(),
                        limitOrder.getPrice()
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

            formattedResponse.append(formattedEvent);
        }
        System.out.println(formattedResponse);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
