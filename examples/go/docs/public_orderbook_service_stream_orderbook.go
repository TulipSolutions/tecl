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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/pub/orderbook"
	"google.golang.org/grpc"
	"io"
	"os"
)

func publicOrderbookServiceStreamOrderbook(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example
	client := orderbook.NewPublicOrderbookServiceClient(conn)

	// Create a request for streaming the BTC_EUR orderbook, with the greatest precision, largest length,
	// and highest update frequency
	// See TBD for semantics of Precision, Length and Frequency
	request := orderbook.StreamOrderbookRequest{
		Market:    orders.Market_BTC_EUR,
		Precision: orderbook.Precision_P3,
		Length:    orderbook.Length_NUM_ENTRIES_100,
		Frequency: orderbook.Frequency_BEST_EFFORT,
	}

	// Create a new cancelable context and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamOrderbook(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PublicOrderbookService.StreamOrderbook error: %v \n", err)
		return
	}

	for {
		entry, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PublicOrderbookService.StreamOrderbook completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PublicOrderbookService.StreamOrderbook error: %v \n", err)
			return
		}
		fmt.Println(entry)
	}
	// CODEINCLUDE-END-MARKER: ref-code-example
}
