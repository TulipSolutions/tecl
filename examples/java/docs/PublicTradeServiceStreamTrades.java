// Copyright 2019 Tulipsolutions B.V.
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
import nl.tulipsolutions.api.pub.PublicTrade;
import nl.tulipsolutions.api.pub.PublicTradeServiceGrpc;
import nl.tulipsolutions.api.pub.PublicTradeServiceGrpc.PublicTradeServiceStub;
import nl.tulipsolutions.api.pub.StreamPublicTradesRequest;

public class PublicTradeServiceStreamTrades {
    public static void run(ManagedChannel channel) {
        PublicTradeServiceStub stub = PublicTradeServiceGrpc.newStub(channel);

        // Create a request for streaming all trades in the BTC_EUR market that occur after initiation of the request
        StreamPublicTradesRequest request = StreamPublicTradesRequest.newBuilder()
            .setMarket(Market.BTC_EUR)
            .build();

        // Make the request asynchronously and add callbacks
        stub.streamTrades(request, new StreamObserver<PublicTrade>() {
            public void onNext(PublicTrade value) {
                System.out.println(value);
            }

            public void onError(Throwable t) {
                System.err.println("PublicTradeService.StreamTrades error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PublicTradeService.StreamTrades completed");
            }
        });
    }
}
