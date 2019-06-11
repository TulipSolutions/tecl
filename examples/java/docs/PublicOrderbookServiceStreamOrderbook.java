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
import nl.tulipsolutions.api.pub.Frequency;
import nl.tulipsolutions.api.pub.Length;
import nl.tulipsolutions.api.pub.OrderbookEntry;
import nl.tulipsolutions.api.pub.Precision;
import nl.tulipsolutions.api.pub.PublicOrderbookServiceGrpc;
import nl.tulipsolutions.api.pub.PublicOrderbookServiceGrpc.PublicOrderbookServiceStub;
import nl.tulipsolutions.api.pub.StreamOrderbookRequest;

public class PublicOrderbookServiceStreamOrderbook {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PublicOrderbookServiceStub stub = PublicOrderbookServiceGrpc.newStub(channel);

        // Create a request for streaming the BTC_EUR orderbook, with the greatest precision, largest length,
        // and highest update frequency
        // See TBD for semantics of Precision, Length and Frequency
        StreamOrderbookRequest request = StreamOrderbookRequest.newBuilder()
            .setMarket(Market.BTC_EUR)
            .setPrecision(Precision.P3)
            .setLength(Length.NUM_ENTRIES_100)
            .setFrequency(Frequency.BEST_EFFORT)
            .build();

        // Make the request asynchronously and add callbacks
        stub.streamOrderbook(request, new StreamObserver<OrderbookEntry>() {
            public void onNext(OrderbookEntry value) {
                System.out.println(value);
            }

            public void onError(Throwable t) {
                System.err.println("PublicOrderbookService.StreamOrderbook error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PublicOrderbookService.StreamOrderbook completed");
            }
        });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }
}
