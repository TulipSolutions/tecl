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
	"crypto/hmac"
	"crypto/sha256"
	"crypto/tls"
	"encoding/base64"
	"fmt"
	"github.com/tulipsolutions/tecl/go/auth"
	"github.com/tulipsolutions/tecl/tulipsolutions/api/common/orders"
	"github.com/tulipsolutions/tecl/tulipsolutions/api/priv/order"
	"github.com/tulipsolutions/tecl/tulipsolutions/api/pub/orderbook"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"io"
	"log"
	"time"
)

// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
const (
	address = "mockgrpc.test.tulipsolutions.nl:443"
)
// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init

// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-request
func streamOrderbook(client orderbook.PublicOrderbookServiceClient, parentContext context.Context) {
	// Create a request for the BTC_EUR orderbook, with the greatest precision, largest length,
	// and highest update frequency
	request := orderbook.StreamOrderbookRequest{
		Market:    orders.Market_BTC_EUR,
		Precision: orderbook.Precision_P3,
		Length:    orderbook.Length_NUM_ENTRIES_100,
		Frequency: orderbook.Frequency_BEST_EFFORT,
	}

	// Create a new cancelable context for a stream orderbook request and make the request synchronously
	ctx, cancel := context.WithCancel(parentContext)
	defer cancel()
	stream, err := client.StreamOrderbook(ctx, &request)
	if err != nil {
		log.Fatalf("PublicOrderbookService.StreamOrderbook error: %v", err)
	}

	// Iterate over the orderbook entries and print the output
	for {
		entry, err := stream.Recv()
		if err == io.EOF {
			log.Println("StreamOrderbook ended")
			break
		}
		if err != nil {
			log.Fatalf("PublicOrderbookService.StreamOrderbook error: %v", err)
		}
		log.Println(entry)
	}
}
// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-request

// CODEINCLUDE-BEGIN-MARKER: getting-started-create-order-request
func createOrder(client order.PrivateOrderServiceClient, parentContext context.Context) {
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

	// Create a new context with a 1s deadline and make the request synchronously
	ctx, cancel := context.WithTimeout(parentContext, time.Second)
	defer cancel()
	response, err := client.CreateOrder(ctx, &request)
	if err != nil {
		log.Fatalf("PrivateOrderService.CreateOrder error: %v", err)
	}
	log.Println(response)
}
// CODEINCLUDE-END-MARKER: getting-started-create-order-request

// CODEINCLUDE-BEGIN-MARKER: getting-started-create-order-authentication
// Subscribe to a public orderbook stream and set a new order
// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
func main() {
// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
	// Create a SHA256 HMAC with the base64 decoded 'secret' string as its key
	secret, err := base64.StdEncoding.DecodeString("secret==")
	if err != nil {
		log.Fatalf("decoding error: %v", err)
		return
	}
	hmacSha256 := hmac.New(sha256.New, secret)
	// CODEINCLUDE-BEGIN-MARKER: dummy-jwt-token-line
	dummyJwt := "eyJraWQiOiI2YzY4OTIzMi03YTcxLTQ3NGItYjBlMi1lMmI1MzMyNDQzOWUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0In0.IL9QJQl55qn3oPsT7sFa7iwd5g1GsEQVr0IO7gCe1UmQdjT7jCIc-pUfjyYUgptPR8HBQl5ncXuBnxwjdXqOMwW1WhPmi_B3BRHQh3Sfu0zNXqKhkuz2-6DffXK1ek3DmK1NpaSikXtg2ruSQ4Uk5xHcnxmXY_SwEij0yot_JRKYEs-0RbyD5Z4jOFKcsbEW46WQmiWdgG3PUKiJT5TfdFd55JM55BwzSOdPIP1S_3dQ4VTDo30mWqAs1KaVbcPqCQmjT1PL0QScTp4w8-YPDcajcafIj98ve9LUoLBLraCIAX34D-hOxu643h9DoG2kIPFfZyXbkDTiUKOl7t-Ykg"
	// CODEINCLUDE-END-MARKER: dummy-jwt-token-line
	// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init

	opts := []grpc.DialOption{
	// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
		// Add an interceptor that signs messages with the provided secret.
		// Only messages to the private API that have a 'signed' field will be signed.
		grpc.WithUnaryInterceptor(auth.CreateMessageAuthInterceptor(hmacSha256)),
		// Add an interceptor that adds a JWT token when a request to a private service is made.
		grpc.WithPerRPCCredentials(auth.TulipAuth{
			Token: dummyJwt,
		}),
		// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
		grpc.WithTransportCredentials(credentials.NewTLS(&tls.Config{})),
	}

	// Set up a connection to the server.
	conn, err := grpc.Dial(address, opts...)
	if err != nil {
		log.Fatalf("could not connect: %v", err)
	}
	defer conn.Close()

	// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
	// Construct clients for accessing PublicOrderbookService and PrivateOrderService using the existing connection.
	// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
	orderbookServiceClient := orderbook.NewPublicOrderbookServiceClient(conn)
	// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
	orderServiceClient := order.NewPrivateOrderServiceClient(conn)
	// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init

	// Create a parent context that can be used in the requests
	mainContext := context.Background()

	// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
	// Stream the orderbook and create an order in go routines
	// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init
	go streamOrderbook(orderbookServiceClient, mainContext)
	// CODEINCLUDE-END-MARKER: getting-started-orderbook-service-init
	go createOrder(orderServiceClient, mainContext)
	// CODEINCLUDE-BEGIN-MARKER: getting-started-orderbook-service-init

	// Wait until cancel
	fmt.Scanln()
}
// CODEINCLUDE-END-MARKER: getting-started-create-order-authentication
