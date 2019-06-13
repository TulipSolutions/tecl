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
import nl.tulipsolutions.api.common.SearchDirection;
import nl.tulipsolutions.api.common.Market;
import nl.tulipsolutions.api.priv.PrivateTrade;
import nl.tulipsolutions.api.priv.PrivateTradeServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateTradeServiceGrpc.PrivateTradeServiceStub;
import nl.tulipsolutions.api.priv.StreamPrivateTradesRequest;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class PrivateTradeServiceStreamTrades {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateTradeServiceStub stub = PrivateTradeServiceGrpc.newStub(channel);

        long start = TimeUnit.SECONDS.toNanos(Instant.now().getEpochSecond());
        // Create a request for streaming all your trades in the BTC_EUR market that occur after start
        StreamPrivateTradesRequest request = StreamPrivateTradesRequest.newBuilder()
            .addMarkets(Market.BTC_EUR)
            .setSearchDirection(SearchDirection.FORWARD)
            .setTimestampNs(start)
            .build();

        // Make the request asynchronously
        stub.streamTrades(request, new StreamObserver<PrivateTrade>() {
            public void onNext(PrivateTrade response) {
                System.out.println(response);
                // CODEINCLUDE-END-MARKER: ref-code-example-request
                parseAndPrint(response);
                // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
            }

            public void onError(Throwable t) {
                System.err.println("PrivateTradeService.StreamTrades error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PrivateTradeService.StreamTrades completed");
            }
        });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(PrivateTrade response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        System.out.println(
            String.format(
                "%s: %s %s %f@%f quote_amount: %f fee: %s %f time: %d id %d matched_orderid: %d",
                response.getClass().getSimpleName(),
                response.getMarket().getValueDescriptor().getName(),
                response.getSide().getValueDescriptor().getName(),
                response.getBaseAmount(),
                response.getPrice(),
                response.getQuoteAmount(),
                response.getFeeCurrency().getValueDescriptor().getName(),
                response.getFee(),
                response.getTimestampNs(),
                response.getEventId(),
                response.getOrderId()
            )
        );
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
