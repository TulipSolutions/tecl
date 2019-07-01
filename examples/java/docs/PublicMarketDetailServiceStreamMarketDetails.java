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
import nl.tulipsolutions.api.pub.MarketDetail;
import nl.tulipsolutions.api.pub.PublicMarketDetailServiceGrpc;
import nl.tulipsolutions.api.pub.PublicMarketDetailServiceGrpc.PublicMarketDetailServiceStub;
import nl.tulipsolutions.api.pub.StreamMarketDetailsRequest;

public class PublicMarketDetailServiceStreamMarketDetails {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PublicMarketDetailServiceStub stub = PublicMarketDetailServiceGrpc.newStub(channel);

        // Create a request for streaming the details for all markets
        StreamMarketDetailsRequest request = StreamMarketDetailsRequest.getDefaultInstance();

        // Make the request asynchronously
        stub.streamMarketDetails(request, new StreamObserver<MarketDetail>() {
            public void onNext(MarketDetail response) {
                System.out.println(response);
                // CODEINCLUDE-END-MARKER: ref-code-example-request
                parseAndPrint(response);
                // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
            }

            public void onError(Throwable t) {
                System.err.println("PublicMarketDetailService.StreamMarketDetails error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PublicMarketDetailService.StreamMarketDetails completed");
            }
        });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(MarketDetail response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        System.out.printf(
            "%s %s %s base currency: %s, quote currency: %s price resolution: %.8f amount resolution: %.8f " +
                "minimum base order amount: %.8f maximum base order amount: %.8f, minimum quote order amount: %.8f " +
                "maximum quote order amount: %.8f\n",
            response.getClass().getSimpleName(),
            response.getMarket(),
            response.getMarketStatus(),
            response.getBase(),
            response.getQuote(),
            response.getPriceResolution(),
            response.getAmountResolution(),
            response.getMinimumBaseOrderAmount(),
            response.getMaximumBaseOrderAmount(),
            response.getMinimumQuoteOrderAmount(),
            response.getMaximumQuoteOrderAmount()
        );
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
