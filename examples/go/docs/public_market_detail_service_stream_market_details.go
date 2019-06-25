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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/pub/market_detail"
	"google.golang.org/grpc"
	"io"
	"os"
)

func publicMarketDetailServiceStreamMarketDetails(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := market_detail.NewPublicMarketDetailServiceClient(conn)

	// Create a request for streaming the details for all markets
	request := market_detail.StreamMarketDetailsRequest{}

	// Create a new cancelable context and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamMarketDetails(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PublicMarketDetailService.StreamMarketDetails error: %v \n", err)
		return
	}

	for {
		response, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PublicMarketDetailService.StreamMarketDetails completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PublicMarketDetailService.StreamMarketDetails error: %v \n", err)
			return
		}
		fmt.Println(response)
		// CODEINCLUDE-END-MARKER: ref-code-example-request
		parseAndPrintMarketDetail(response)
		// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	}
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}

func parseAndPrintMarketDetail(detail *market_detail.MarketDetail) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	fmt.Printf(
		"%T %s %s base currency: %s, quote currency: %s price resolution: %.8f amount resolution: %.8f "+
			"minimum base order amount: %.8f maximum base order amount: %.8f, minimum quote order amount: %.8f "+
			"maximum quote order amount: %.8f\n",
		detail,
		detail.Market.String(),
		detail.MarketStatus.String(),
		detail.Base.String(),
		detail.Quote.String(),
		detail.PriceResolution,
		detail.AmountResolution,
		detail.GetMinimumBaseOrderAmount(),
		detail.GetMaximumBaseOrderAmount(),
		detail.GetMinimumQuoteOrderAmount(),
		detail.GetMaximumQuoteOrderAmount(),
	)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
