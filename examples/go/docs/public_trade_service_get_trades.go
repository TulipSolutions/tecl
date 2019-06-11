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
	"os"
	"time"
)

func publicTradeServiceGetTrades(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := trade.NewPublicTradeServiceClient(conn)

	// Create a request for the most recent trades in the BTC_EUR market
	request := trade.GetPublicTradesRequest{
		Market: orders.Market_BTC_EUR,
	}

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.GetTrades(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PublicTradeService.GetTrades error: %v \n", err)
		return
	}
	fmt.Println(response)
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}
