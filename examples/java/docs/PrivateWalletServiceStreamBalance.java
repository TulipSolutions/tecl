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
import nl.tulipsolutions.api.priv.BalanceResponse;
import nl.tulipsolutions.api.priv.PrivateWalletServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateWalletServiceGrpc.PrivateWalletServiceStub;
import nl.tulipsolutions.api.priv.StreamBalanceRequest;

public class PrivateWalletServiceStreamBalance {
    public static void run(ManagedChannel channel) {
        PrivateWalletServiceStub stub = PrivateWalletServiceGrpc.newStub(channel);

        // Create a request for streaming your balances for all currencies
        StreamBalanceRequest request = StreamBalanceRequest.getDefaultInstance();

        // Make the request asynchronously
        stub.streamBalance(request, new StreamObserver<BalanceResponse>() {
            public void onNext(BalanceResponse value) {
                System.out.println(value);
            }

            public void onError(Throwable t) {
                System.err.println("PrivateWalletService.StreamBalance error: " + t.getMessage());
            }

            public void onCompleted() {
                System.out.println("PrivateWalletService.StreamBalance completed");
            }
        });
    }
}
