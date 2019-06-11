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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/priv/order"
	"google.golang.org/grpc"
	"os"
	"time"
)

func privateOrderServiceCreateOrder(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := order.NewPrivateOrderServiceClient(conn)

	// CODEINCLUDE-BEGIN-MARKER: authentication-request
	// Create a request for a new order with an orderId that is the nanos since unix epoch
	orderId := uint64(time.Now().UnixNano())
	request := order.CreateOrderRequest{
		Market: orders.Market_BTC_EUR,
		OrderType: &order.CreateOrderRequest_LimitOrder{
			LimitOrder: &order.LimitOrderRequest{
				Side:       orders.Side_BUY,
				BaseAmount: 1.0,
				Price:      3000,
			},
		},
		Tonce: orderId,
	}
	// CODEINCLUDE-END-MARKER: authentication-request

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.CreateOrder(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PrivateOrderService.CreateOrder error: %v \n", err)
		return
	}
	fmt.Println(response)
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}
