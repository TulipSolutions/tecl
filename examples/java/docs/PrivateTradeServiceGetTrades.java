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
import nl.tulipsolutions.api.priv.GetPrivateTradesRequest;
import nl.tulipsolutions.api.priv.PrivateTrade;
import nl.tulipsolutions.api.priv.PrivateTradeServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateTradeServiceGrpc.PrivateTradeServiceStub;
import nl.tulipsolutions.api.priv.PrivateTrades;

import java.util.concurrent.TimeUnit;

public class PrivateTradeServiceGetTrades {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateTradeServiceStub stub = PrivateTradeServiceGrpc.newStub(channel);

        // Create a request for your 10 most recent trades in the BTC_EUR market
        GetPrivateTradesRequest getPrivateTradesRequest = GetPrivateTradesRequest.newBuilder()
            .addMarkets(Market.BTC_EUR)
            .setSearchDirection(SearchDirection.BACKWARD)
            .setLimit(10)
            .build();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getTrades(getPrivateTradesRequest, new StreamObserver<PrivateTrades>() {
                public void onNext(PrivateTrades response) {
                    System.out.println(response);
                    // CODEINCLUDE-END-MARKER: ref-code-example-request
                    parseAndPrint(response);
                    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateTradeService.GetTrades error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateTradeService.GetTrades completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(PrivateTrades response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        String result_string = String.format("%s\n", response.getClass().getSimpleName());
        for (PrivateTrade trade : response.getTradesList()) {
            result_string +=
                String.format(
                    "\t%s: %s %s %f@%f quote_amount: %f fee: %s %f time: %d id %d matched_orderid: %d\n",
                    trade.getClass().getSimpleName(),
                    trade.getMarket().getValueDescriptor().getName(),
                    trade.getSide().getValueDescriptor().getName(),
                    trade.getBaseAmount(),
                    trade.getPrice(),
                    trade.getQuoteAmount(),
                    trade.getFeeCurrency().getValueDescriptor().getName(),
                    trade.getFee(),
                    trade.getTimestampNs(),
                    trade.getEventId(),
                    trade.getOrderId()
                );
        }
        System.out.println(result_string);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
