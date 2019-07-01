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
import nl.tulipsolutions.api.pub.Interval;
import nl.tulipsolutions.api.pub.OhlcBin;
import nl.tulipsolutions.api.pub.PublicOhlcServiceGrpc;
import nl.tulipsolutions.api.pub.PublicOhlcServiceGrpc.PublicOhlcServiceStub;
import nl.tulipsolutions.api.pub.StreamOhlcRequest;

import java.util.Arrays;

public class PublicOhlcServiceStreamOhlcData {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PublicOhlcServiceStub stub = PublicOhlcServiceGrpc.newStub(channel);

        // Create a request for the OhlcBins for specified market and intervals.
        StreamOhlcRequest request = StreamOhlcRequest.newBuilder()
            .setMarket(Market.BTC_EUR)
            .addAllIntervals(
                Arrays.asList(
                    Interval.ONE_SECOND,
                    Interval.ONE_MINUTE,
                    Interval.FIVE_MINUTES
                )
            )
            .build();

        // Make the request asynchronously and add callbacks
        stub.streamOhlcData(request, new StreamObserver<OhlcBin>() {
            public void onNext(OhlcBin response) {
                System.out.println(response);
                // CODEINCLUDE-END-MARKER: ref-code-example-request
                parseAndPrint(response);
                // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
            }

            public void onError(Throwable t) {
                System.err.println("PublicOhlcService.StreamOhlcData error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PublicOhlcService.StreamOhlcData completed");
            }
        });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(OhlcBin response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        System.out.printf(
            "%s %d %s open: %f, high: %f low: %f close: %f volume_base: %f volume_quote: %f nr_trades: %d\n",
            response.getClass().getSimpleName(),
            response.getTimestampNs(),
            response.getInterval(),
            response.getOpen(),
            response.getHigh(),
            response.getLow(),
            response.getClose(),
            response.getVolumeBase(),
            response.getVolumeQuote(),
            response.getNumberOfTrades()
        );
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
