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
import nl.tulipsolutions.api.pub.GetOrderbookRequest;
import nl.tulipsolutions.api.pub.Length;
import nl.tulipsolutions.api.pub.OrderbookEntries;
import nl.tulipsolutions.api.pub.OrderbookEntry;
import nl.tulipsolutions.api.pub.Precision;
import nl.tulipsolutions.api.pub.PublicOrderbookServiceGrpc;
import nl.tulipsolutions.api.pub.PublicOrderbookServiceGrpc.PublicOrderbookServiceStub;

import java.util.concurrent.TimeUnit;

public class PublicOrderbookServiceGetOrderbook {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PublicOrderbookServiceStub stub = PublicOrderbookServiceGrpc.newStub(channel);

        // Create a request for the BTC_EUR orderbook, with the greatest precision and largest length
        // See TBD for semantics of Precision and Length
        GetOrderbookRequest request = GetOrderbookRequest.newBuilder()
            .setMarket(Market.BTC_EUR)
            .setPrecision(Precision.P3)
            .setLength(Length.NUM_ENTRIES_100)
            .build();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getOrderbook(request, new StreamObserver<OrderbookEntries>() {
                public void onNext(OrderbookEntries response) {
                    System.out.println(response);
                    // CODEINCLUDE-END-MARKER: ref-code-example-request
                    parseAndPrint(response);
                    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
                }

                public void onError(Throwable t) {
                    System.err.println("PublicOrderbookService.GetOrderbook error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PublicOrderbookService.GetOrderbook completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(OrderbookEntries response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        StringBuilder formattedResponse = new StringBuilder(String.format("%s\n", response.getClass().getSimpleName()));
        for (OrderbookEntry detail : response.getEntriesList()) {
            formattedResponse.append(
                String.format(
                    "\t%s %s %d orders @ %f total %f\n",
                    detail.getClass().getSimpleName(),
                    detail.getSide(),
                    detail.getOrdersAtPriceLevel(),
                    detail.getPriceLevel(),
                    detail.getAmount()
                )
            );
        }
        System.out.println(formattedResponse);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
