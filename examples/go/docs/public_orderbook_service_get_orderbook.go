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
	"os"
	"time"
)

func publicOrderbookServiceGetOrderbook(conn *grpc.ClientConn, parentContext context.Context) {
	client := orderbook.NewPublicOrderbookServiceClient(conn)

	// Create a request for the BTC_EUR orderbook, with the greatest precision and largest length
	// See TBD for semantics of Precision and Length
	request := orderbook.GetOrderbookRequest{
		Market:    orders.Market_BTC_EUR,
		Precision: orderbook.Precision_P3,
		Length:    orderbook.Length_NUM_ENTRIES_100,
	}

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.GetOrderbook(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PublicOrderbookService.GetOrderbook error: %v \n", err)
		return
	}
	fmt.Println(response)
}
