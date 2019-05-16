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
		entry, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PublicMarketDetailService.StreamMarketDetails completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PublicMarketDetailService.StreamMarketDetails error: %v \n", err)
			return
		}
		fmt.Println(entry)
	}
}
