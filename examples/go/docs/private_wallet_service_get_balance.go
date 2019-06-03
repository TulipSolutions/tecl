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
	"github.com/tulipsolutions/tecl/tulipsolutions/api/priv/wallet"
	"google.golang.org/grpc"
	"os"
	"time"
)

func privateWalletServiceGetBalance(conn *grpc.ClientConn, parentContext context.Context) {
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
	client := wallet.NewPrivateWalletServiceClient(conn)

	// Create a request for your balances for all currencies
	request := wallet.GetBalanceRequest{}

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.GetBalance(ctx, &request)
	if err != nil {
		_, _ = fmt.Fprintf(os.Stderr, "PrivateWalletService.GetBalance error: %v \n", err)
		return
	}
	fmt.Println(response)
	// CODEINCLUDE-END-MARKER: ref-code-example-request
	// CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
	resultString := fmt.Sprintf("%T\n", response)
	for _, balance := range response.BalanceResponse {
		resultString += fmt.Sprintf(
			"\t%T %s total: %f locked: %f\n",
			balance,
			balance.Currency.String(),
			balance.TotalAmount,
			balance.LockedAmount,
		)
	}
	fmt.Printf(resultString)
	// CODEINCLUDE-END-MARKER: ref-code-example-response
}
