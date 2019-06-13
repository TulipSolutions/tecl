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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/priv/trade"
	"google.golang.org/grpc"
	"os"
	"time"
)

func privateTradeServiceGetTrades(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := trade.NewPrivateTradeServiceClient(conn)

	// Create a request for your 10 most recent trades in the BTC_EUR market
	request := trade.GetPrivateTradesRequest{
		Markets:         []orders.Market{orders.Market_BTC_EUR},
		SearchDirection: orders.SearchDirection_BACKWARD,
		Limit:           10,
	}

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.GetTrades(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PrivateTradeService.GetTrades error: %v \n", err)
		return
	}
	fmt.Println(response)
	// CODEINCLUDE-END-MARKER: ref-code-example-request
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	resultString := fmt.Sprintf("%T\n", response)
	for _, trade := range response.Trades {
		resultString += fmt.Sprintf(
			"\t%T: %s %s %f@%f quote_amount: %f fee: %s %f time: %d id: %d matched_orderid: %d\n",
			trade,
			trade.Market.String(),
			trade.Side.String(),
			trade.BaseAmount,
			trade.Price,
			trade.QuoteAmount,
			trade.FeeCurrency.String(),
			trade.Fee,
			trade.TimestampNs,
			trade.EventId,
			trade.OrderId,
		)
	}
	fmt.Printf(resultString)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
