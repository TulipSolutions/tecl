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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/pub/trade"
	"google.golang.org/grpc"
	"io"
	"os"
)

func publicTradeServiceStreamTrades(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := trade.NewPublicTradeServiceClient(conn)

	// Create a request for streaming all trades in the BTC_EUR market that occur after initiation of the request
	request := trade.StreamPublicTradesRequest{
		Market: orders.Market_BTC_EUR,
	}

	// Create a new cancelable context and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamTrades(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PublicTradeService.StreamTrades error: %v \n", err)
		return
	}

	for {
		response, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PublicTradeService.StreamTrades completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PublicTradeService.StreamTrades error: %v \n", err)
			return
		}
		fmt.Println(response)
		// CODEINCLUDE-END-MARKER: ref-code-example-request
		parseAndPrintTrade(response)
		// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	}
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}
func parseAndPrintTrade(trade *trade.PublicTrade) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	fmt.Printf(
		"%T: %s %s %f@%f quote_amount: %f time: %d id: %d\n",
		trade,
		trade.Market.String(),
		trade.Side.String(),
		trade.BaseAmount,
		trade.Price,
		trade.QuoteAmount,
		trade.TimestampNs,
		trade.EventId,
	)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
