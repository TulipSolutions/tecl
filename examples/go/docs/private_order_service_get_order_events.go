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
	"os"
	"time"
)

func privateOrderServiceGetOrderEvents(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := order.NewPrivateOrderServiceClient(conn)

	// Create a request for 10 most recent order events on the BTC_EUR and BTC_USD markets
	request := order.GetOrderEventsRequest{
		Markets:         []orders.Market{orders.Market_BTC_EUR, orders.Market_BTC_USD},
		SearchDirection: orders.SearchDirection_BACKWARD,
		Limit:           10,
	}

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.GetOrderEvents(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PrivateOrderService.GetOrderEvents error: %v \n", err)
		return
	}
	fmt.Println(response)
	// CODEINCLUDE-END-MARKER: ref-code-example-request
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	resultString := fmt.Sprintf("%T\n", response)
	for _, event := range response.Events {
		switch event.GetEvent().(type) {
		case *order.OrderEvent_CreateOrderEvent:
			orderEvent := event.GetCreateOrderEvent()
			switch orderEvent.GetOrderType().(type) {
			case *order.CreateOrderEvent_CreateLimitOrder:
				limitOrderEvent := orderEvent.GetCreateLimitOrder()
				resultString += fmt.Sprintf(
					"\t%T: Event %d order %d on market %s limit %s %f@%f\n",
					limitOrderEvent,
					event.GetEventId(),
					event.GetOrderId(),
					event.GetMarket(),
					limitOrderEvent.GetSide(),
					limitOrderEvent.GetBaseAmount(),
					limitOrderEvent.GetPrice(),
				)
			}
		case *order.OrderEvent_FillOrderEvent:
			fillOrderEvent := event.GetFillOrderEvent()
			resultString += fmt.Sprintf(
				"\t%T: Event %d order %d on market %s %s %f@%f\n",
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
			resultString += fmt.Sprintf(
				"\t%T: Event %d order %d on market %s\n",
				cancelOrderEvent,
				event.GetEventId(),
				event.GetOrderId(),
				event.GetMarket(),
			)
		}
	}
	fmt.Printf(resultString)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
