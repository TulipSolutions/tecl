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

package main

import (
	"context"
	"fmt"
	"github.com/tulipsolutions/tecl/tulipsolutions/api/common/orders"
	"github.com/tulipsolutions/tecl/tulipsolutions/api/pub/ohlc"
	"google.golang.org/grpc"
	"io"
	"os"
)

func publicOhlcServiceStreamOhlcData(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := ohlc.NewPublicOhlcServiceClient(conn)

	// Create a request for streaming ohlcBins for the specified market and intervals.
	request := ohlc.StreamOhlcRequest{
		Market:    orders.Market_BTC_EUR,
		Intervals: []ohlc.Interval{ohlc.Interval_ONE_SECOND, ohlc.Interval_ONE_MINUTE, ohlc.Interval_FIVE_MINUTES},
	}

	// Create a new cancelable context and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamOhlcData(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PublicOhlcService.StreamOhlcData error: %v \n", err)
		return
	}

	for {
		response, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PublicOhlcService.StreamOhlcData completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PublicOhlcService.StreamOhlcData error: %v \n", err)
			return
		}
		fmt.Println(response)
		// CODEINCLUDE-END-MARKER: ref-code-example-request
		parseAndPrintOhlc(response)
		// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	}
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}

func parseAndPrintOhlc(bin *ohlc.OhlcBin) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	fmt.Printf(
		"%T %d %s open: %f, high: %f low: %f close: %f volume_base: %f volume_quote: %f nr_trades: %d\n",
		bin,
		bin.TimestampNs,
		bin.Interval.String(),
		bin.Open,
		bin.High,
		bin.Low,
		bin.Close,
		bin.VolumeBase,
		bin.VolumeQuote,
		bin.NumberOfTrades,
	)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
