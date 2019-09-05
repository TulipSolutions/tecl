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
import nl.tulipsolutions.api.priv.CreateOrderEvent;
import nl.tulipsolutions.api.priv.FillOrderEvent;
import nl.tulipsolutions.api.priv.OrderEvent;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateOrderServiceGrpc.PrivateOrderServiceStub;
import nl.tulipsolutions.api.priv.StreamOrderEventsRequest;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PrivateOrderServiceStreamOrderEvents {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateOrderServiceStub stub = PrivateOrderServiceGrpc.newStub(channel);

        long start = TimeUnit.SECONDS.toNanos(Instant.now().getEpochSecond());
        // Create a request for streaming all your order events in the BTC_EUR market that occur after start
        StreamOrderEventsRequest request = StreamOrderEventsRequest.newBuilder()
            .addMarkets(Market.BTC_EUR)
            .setSearchDirection(SearchDirection.FORWARD)
            .setTimestampNs(start)
            .build();

        // Make the request asynchronously
        stub.streamOrderEvents(request, new StreamObserver<OrderEvent>() {
            public void onNext(OrderEvent response) {
                System.out.println(response);
                // CODEINCLUDE-END-MARKER: ref-code-example-request
                parseAndPrint(response);
                // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
            }

            public void onError(Throwable t) {
                System.err.println("PrivateOrderService.StreamOrderEvents error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PrivateOrderService.StreamOrderEvents completed");
            }
        });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(OrderEvent event) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        String formattedEvent;
        switch (event.getEventCase()) {
            case CREATE_ORDER_EVENT:
                CreateOrderEvent createOrderEvent = event.getCreateOrderEvent();
                String formattedDeadline;
                if (createOrderEvent.getDeadlineNs() != 0) {
                    formattedDeadline = String.format("deadline @ %d", createOrderEvent.getDeadlineNs());
                } else {
                    formattedDeadline = "(no deadline)";
                }
                switch (createOrderEvent.getOrderTypeCase()) {
                    case CREATE_LIMIT_ORDER:
                        CreateLimitOrderEvent createLimitOrderEvent = createOrderEvent.getCreateLimitOrder();
                        formattedEvent = String.format(
                            "%s: Event %d order %d on market %s limit %s %f@%f %s\n",
                            createLimitOrderEvent.getClass().getSimpleName(),
                            event.getEventId(),
                            event.getOrderId(),
                            event.getMarket(),
                            createLimitOrderEvent.getSide(),
                            createLimitOrderEvent.getBaseAmount(),
                            createLimitOrderEvent.getPrice(),
                            formattedDeadline
                        );
                        break;
                    default:
                        formattedEvent = "";
                        break;
                }
                break;
            case FILL_ORDER_EVENT:
                FillOrderEvent fillOrderEvent = event.getFillOrderEvent();
                formattedEvent = String.format(
                    "%s: Event %d order %d on market %s %s %f@%f",
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
                    "%s: Event %d order %d on market %s",
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

        System.out.println(formattedEvent);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
