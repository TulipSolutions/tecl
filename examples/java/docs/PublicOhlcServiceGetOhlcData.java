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
import nl.tulipsolutions.api.pub.GetOhlcRequest;
import nl.tulipsolutions.api.pub.GetOhlcResponse;
import nl.tulipsolutions.api.pub.Interval;
import nl.tulipsolutions.api.pub.OhlcBin;
import nl.tulipsolutions.api.pub.PublicOhlcServiceGrpc;
import nl.tulipsolutions.api.pub.PublicOhlcServiceGrpc.PublicOhlcServiceStub;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class PublicOhlcServiceGetOhlcData {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PublicOhlcServiceStub stub = PublicOhlcServiceGrpc.newStub(channel);

        // Create a request for the OhlcBins for specified market and intervals.
        GetOhlcRequest request = GetOhlcRequest.newBuilder()
            .setMarket(Market.BTC_EUR)
            .addAllIntervals(
                Arrays.asList(
                    Interval.ONE_SECOND,
                    Interval.ONE_MINUTE,
                    Interval.FIVE_MINUTES
                )
            )
            .build();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getOhlcData(request, new StreamObserver<GetOhlcResponse>() {
                public void onNext(GetOhlcResponse response) {
                    System.out.println(response);
                    // CODEINCLUDE-END-MARKER: ref-code-example-request
                    parseAndPrint(response);
                    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
                }

                public void onError(Throwable t) {
                    System.err.println("PublicOhlcService.GetOhlcData error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PublicOhlcService.GetOhlcData completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(GetOhlcResponse response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        StringBuilder formattedResponse = new StringBuilder(response.getClass().getSimpleName());
        for (OhlcBin detail : response.getBinsList()) {
            formattedResponse.append(
                String.format(
                    "\t%s %d %s open: %f, high: %f low: %f close: %f volume_base: %f volume_quote: %f nr_trades: %d\n",
                    detail.getClass().getSimpleName(),
                    detail.getTimestampNs(),
                    detail.getInterval(),
                    detail.getOpen(),
                    detail.getHigh(),
                    detail.getLow(),
                    detail.getClose(),
                    detail.getVolumeBase(),
                    detail.getVolumeQuote(),
                    detail.getNumberOfTrades()
                )
            );
        }
        System.out.println(formattedResponse);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
