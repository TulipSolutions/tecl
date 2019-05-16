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
import nl.tulipsolutions.api.priv.PrivateTrade;
import nl.tulipsolutions.api.priv.PrivateTradeServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateTradeServiceGrpc.PrivateTradeServiceStub;
import nl.tulipsolutions.api.priv.StreamPrivateTradesRequest;

public class PrivateTradeServiceStreamTrades {
    public static void run(ManagedChannel channel) {
        PrivateTradeServiceStub stub = PrivateTradeServiceGrpc.newStub(channel);

        // Create a request for streaming all your trades in the BTC_EUR that occur after initiation of the request
        StreamPrivateTradesRequest request = StreamPrivateTradesRequest.newBuilder()
            .setMarket(Market.BTC_EUR)
            .build();

        // Make the request asynchronously
        stub.streamTrades(request, new StreamObserver<PrivateTrade>() {
            public void onNext(PrivateTrade value) {
                System.out.println(value);
            }

            public void onError(Throwable t) {
                System.err.println("PrivateTradeService.StreamTrades error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PrivateTradeService.StreamTrades completed");
            }
        });
    }
}
