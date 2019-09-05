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
	"io"
	"os"
)

func privateActiveOrdersServiceStreamActiveOrders(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := order.NewPrivateActiveOrdersServiceClient(conn)

	// Create a request for streaming all your active orders
	// no fields are set as it does not have any
	request := order.StreamActiveOrdersRequest{}

	// Create a new cancelable context and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamActiveOrders(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PrivateActiveOrdersService.StreamActiveOrders error: %v \n", err)
		return
	}

	for {
		response, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PrivateActiveOrdersService.StreamActiveOrders completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PrivateActiveOrdersService.StreamActiveOrders error: %v \n", err)
			return
		}
		fmt.Println(response)
		// CODEINCLUDE-END-MARKER: ref-code-example-request
		parseAndPrintPrivateOrder(response)
		// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	}
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}

func parseAndPrintPrivateOrder(response *order.ActiveOrderStatus) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	var orderTypeDetail string
	var deadline string
	switch activeOrder := response.Order.(type) {
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
	if response.DeadlineNs != 0 {
		deadline = fmt.Sprintf("deadline @ %d", response.DeadlineNs)
	} else {
		deadline = "(no deadline)"
	}
	fmt.Printf("%T: %d for market %s %s %s\n",
		response,
		response.OrderId,
		response.Market.String(),
		orderTypeDetail,
		deadline,
	)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
