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

import java.util.Arrays;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import nl.tulipsolutions.api.common.Market;
import nl.tulipsolutions.api.pub.GetOhlcResponse;
import nl.tulipsolutions.api.pub.Interval;
import nl.tulipsolutions.api.pub.GetOhlcRequest;
import nl.tulipsolutions.api.pub.PublicOhlcServiceGrpc;
import nl.tulipsolutions.api.pub.PublicOhlcServiceGrpc.PublicOhlcServiceStub;
import nl.tulipsolutions.api.pub.Ohlc;

import java.util.concurrent.TimeUnit;

public class PublicOhlcServiceGetOhlcData {
    public static void run(ManagedChannel channel) {
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
                public void onNext(GetOhlcResponse value) {
                    System.out.println(value);
                }

                public void onError(Throwable t) {
                    System.err.println("PublicOhlcService.GetOhlcData error: " + t.getMessage());
                }

                public void onCompleted() {
                    System.out.println("PublicOhlcService.GetOhlcData completed");
                }
            });
    }
}
