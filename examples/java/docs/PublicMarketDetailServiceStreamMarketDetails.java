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
import nl.tulipsolutions.api.pub.StreamMarketDetailsRequest;
import nl.tulipsolutions.api.pub.PublicMarketDetailServiceGrpc;
import nl.tulipsolutions.api.pub.PublicMarketDetailServiceGrpc.PublicMarketDetailServiceStub;

public class PublicMarketDetailServiceStreamMarketDetails {
    public static void run(ManagedChannel channel) {
        PublicMarketDetailServiceStub stub = PublicMarketDetailServiceGrpc.newStub(channel);

        // Create a request for streaming the details for all markets
        StreamMarketDetailsRequest request = StreamMarketDetailsRequest.getDefaultInstance();

        // Make the request asynchronously
        stub.streamMarketDetails(request, new StreamObserver<MarketDetail>() {
            public void onNext(MarketDetail value) {
                System.out.println(value);
            }

            public void onError(Throwable t) {
                System.err.println("PublicMarketDetailService.StreamMarketDetails error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PublicMarketDetailService.StreamMarketDetails completed");
            }
        });
    }
}
