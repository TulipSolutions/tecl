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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/priv/order"
	"google.golang.org/grpc"
	"os"
	"time"
)

func privateActiveOrdersServiceGetActiveOrders(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := order.NewPrivateActiveOrdersServiceClient(conn)

	// Create a request for all your active orders
	// no fields are set as it does not have any
	request := order.GetActiveOrdersRequest{}

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.GetActiveOrders(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PrivateActiveOrdersService.GetActiveOrders error: %v \n", err)
		return
	}
	fmt.Println(response)
	// CODEINCLUDE-END-MARKER: ref-code-example-request
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	resultString := fmt.Sprintf("%T\n", response)
	for _, activeOrder := range response.Orders {
		var orderTypeDetail string
		switch activeOrder := activeOrder.Order.(type) {
		case *order.ActiveOrderStatus_LimitOrder:
			orderTypeDetail = fmt.Sprintf("%s %f@%f remaining %f",
				activeOrder.LimitOrder.Side.String(),
				activeOrder.LimitOrder.BaseAmount,
				activeOrder.LimitOrder.Price,
				activeOrder.LimitOrder.BaseRemaining,
			)
		default:
			orderTypeDetail = "was removed from orderbook"
		}
		resultString += fmt.Sprintf("\t%T: %d for market %s %s\n",
			activeOrder,
			activeOrder.OrderId,
			activeOrder.Market.String(),
			orderTypeDetail,
		)
	}
	fmt.Println(resultString)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
