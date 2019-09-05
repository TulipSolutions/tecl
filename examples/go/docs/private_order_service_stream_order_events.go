/*
 * Copyright 2019 Tulip Solutions B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package main

import (
	"context"
	"fmt"
	"github.com/tulipsolutions/tecl/tulipsolutions/api/common/orders"
	"github.com/tulipsolutions/tecl/tulipsolutions/api/priv/order"
	"google.golang.org/grpc"
	"io"
	"os"
	"time"
)

func privateOrderServiceStreamOrderEvents(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := order.NewPrivateOrderServiceClient(conn)

	start := uint64(time.Now().UnixNano())
	// Create a request for streaming all your order events in the BTC_EUR market that occur after start
	request := order.StreamOrderEventsRequest{
		Markets:         []orders.Market{orders.Market_BTC_EUR},
		SearchDirection: orders.SearchDirection_FORWARD,
		Start: &order.StreamOrderEventsRequest_TimestampNs{
			TimestampNs: start,
		},
	}

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamOrderEvents(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PrivateOrderService.GetOrderEvents error: %v \n", err)
		return
	}

	for {
		response, err := stream.Recv()
		if err == io.EOF {
			fmt.Println("PrivateTradeService.StreamTrades completed")
			return
		}
		if err != nil {
			_, _ = fmt.Fprintf(os.Stderr, "PrivateTradeService.StreamTrades error: %v \n", err)
			return
		}
		fmt.Println(response)
		// CODEINCLUDE-END-MARKER: ref-code-example-request
		parseAndPrintOrderEvent(response)
		// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	}
	// CODEINCLUDE-END-MARKER: ref-code-example-request
}

func parseAndPrintOrderEvent(event *order.OrderEvent) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	switch event.GetEvent().(type) {
	case *order.OrderEvent_CreateOrderEvent:
		orderEvent := event.GetCreateOrderEvent()
		var deadline string
		if orderEvent.DeadlineNs != 0 {
			deadline = fmt.Sprintf("deadline @ %d", orderEvent.DeadlineNs)
		} else {
			deadline = "(no deadline)"
		}
		switch orderEvent.GetOrderType().(type) {
		case *order.CreateOrderEvent_CreateLimitOrder:
			limitOrderEvent := orderEvent.GetCreateLimitOrder()
			fmt.Printf(
				"\t%T: Event %d order %d on market %s limit %s %f@%f %s\n",
				limitOrderEvent,
				event.GetEventId(),
				event.GetOrderId(),
				event.GetMarket(),
				limitOrderEvent.GetSide(),
				limitOrderEvent.GetBaseAmount(),
				limitOrderEvent.GetPrice(),
				deadline,
			)
		case *order.CreateOrderEvent_CreateMarketOrder:
			marketOrderEvent := orderEvent.GetCreateMarketOrder()
			fmt.Printf(
				"\t%T: Event %d order %d on market %s market %s %f %s\n",
				marketOrderEvent,
				event.GetEventId(),
				event.GetOrderId(),
				event.GetMarket(),
				marketOrderEvent.GetSide(),
				marketOrderEvent.GetBaseAmount(),
				deadline,
			)
		}
	case *order.OrderEvent_FillOrderEvent:
		fillOrderEvent := event.GetFillOrderEvent()
		fmt.Printf(
			"%T: Event %d order %d on market %s %s %f@%f\n",
			fillOrderEvent,
			event.GetEventId(),
			event.GetOrderId(),
			event.GetMarket(),
			fillOrderEvent.GetSide(),
			fillOrderEvent.GetBaseAmount(),
			fillOrderEvent.GetPrice(),
		)
	case *order.OrderEvent_CancelOrderEvent:
		cancelOrderEvent := event.GetCancelOrderEvent()
		fmt.Printf(
			"%T: Event %d order %d on market %s\n",
			cancelOrderEvent,
			event.GetEventId(),
			event.GetOrderId(),
			event.GetMarket(),
		)
	}
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
