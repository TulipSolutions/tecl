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
import nl.tulipsolutions.api.priv.BalanceResponse;
import nl.tulipsolutions.api.priv.BalanceSnapshot;
import nl.tulipsolutions.api.priv.GetBalanceRequest;
import nl.tulipsolutions.api.priv.PrivateWalletServiceGrpc;
import nl.tulipsolutions.api.priv.PrivateWalletServiceGrpc.PrivateWalletServiceStub;

import java.util.concurrent.TimeUnit;

public class PrivateWalletServiceGetBalance {
    public static void run(ManagedChannel channel) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
        PrivateWalletServiceStub stub = PrivateWalletServiceGrpc.newStub(channel);

        // Create a request for your balances for all currencies
        GetBalanceRequest request = GetBalanceRequest.getDefaultInstance();

        // Make the request asynchronously with a one second deadline
        stub.withDeadlineAfter(1, TimeUnit.SECONDS)
            .getBalance(request, new StreamObserver<BalanceSnapshot>() {
                public void onNext(BalanceSnapshot response) {
                    System.out.println(response);
                    // CODEINCLUDE-END-MARKER: ref-code-example-request
                    parseAndPrint(response);
                    // CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
                }

                public void onError(Throwable t) {
                    System.err.println("PrivateWalletService.GetBalance error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PrivateWalletService.GetBalance completed");
                }
            });
        // CODEINCLUDE-END-MARKER: ref-code-example-request
    }

    public static void parseAndPrint(BalanceSnapshot response) {
        // CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
        String result_string = String.format("%s\n", response.getClass().getSimpleName());
        for (BalanceResponse balance : response.getBalanceResponseList()) {
            result_string +=
                String.format(
                    "\t%s %s total: %f locked: %f\n",
                    balance.getClass().getSimpleName(),
                    balance.getCurrency().getValueDescriptor().getName(),
                    balance.getTotalAmount(),
                    balance.getLockedAmount()
                );
        }
        System.out.println(result_string);
        // CODEINCLUDE-END-MARKER: ref-code-example-response
    }
}
