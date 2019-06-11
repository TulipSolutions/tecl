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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/pub/ticker"
	"google.golang.org/grpc"
	"io"
	"os"
)

func publicTickerServiceStreamTickers(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := ticker.NewPublicTickerServiceClient(conn)

	// Create a request for streaming the tickers for all markets
	request := ticker.StreamTickersRequest{}

	// Create a new cancelable context and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamTickers(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PublicTickerService.StreamTickers error: %v \n", err)
		return
	}

	for {
		entry, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PublicTickerService.StreamTickers completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PublicTickerService.StreamTickers error: %v \n", err)
			return
		}
		fmt.Println(entry)
	}
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}
