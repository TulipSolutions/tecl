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
import nl.tulipsolutions.api.pub.GetMarketDetailsRequest;
import nl.tulipsolutions.api.pub.MarketDetails;
import nl.tulipsolutions.api.pub.PublicMarketDetailServiceGrpc;
import nl.tulipsolutions.api.pub.PublicMarketDetailServiceGrpc.PublicMarketDetailServiceStub;

import java.util.concurrent.TimeUnit;

public class PublicMarketDetailServiceGetMarketDetails {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PublicMarketDetailServiceStub stub = PublicMarketDetailServiceGrpc.newStub(channel);

        // Create a request for the details for all markets
        GetMarketDetailsRequest request = GetMarketDetailsRequest.getDefaultInstance();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getMarketDetails(request, new StreamObserver<MarketDetails>() {
                public void onNext(MarketDetails value) {
                    System.out.println(value);
                }

                public void onError(Throwable t) {
                    System.err.println("PublicMarketDetailService.GetMarketDetails error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PublicMarketDetailService.GetMarketDetails completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }
}
