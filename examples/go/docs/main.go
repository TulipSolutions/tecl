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
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"log"
	"os"
)

// Subscribe to a public orderbook stream and set a new order
func main() {
	var args = os.Args
	var address string
	var creds credentials.TransportCredentials
	var err error

	switch len(args) {
	case 1:
		// Use system CA trust store to connect to public MockGrpc service.
		address = "mockgrpc.test.tulipsolutions.nl:443"
		creds = credentials.NewTLS(&tls.Config{})
	case 3:
		// Use Mock CA certificates from this repository
		// The server cert is set up to accept connections to localhost
		caCertPath := "mockgrpc/src/main/resources/certs/mock_ca.crt"
		if _, err := os.Stat(caCertPath); os.IsNotExist(err) {
			// If this file is run from the examples workspace, the cert file will be placed here by Bazel
			caCertPath = "external/nl_tulipsolutions_tecl/" + caCertPath
		}
		creds, err = credentials.NewClientTLSFromFile(caCertPath, "")
		address = fmt.Sprintf("%s:%s", args[1], args[2])
	case 4:
		// Use command line provided CA certificate bundle
		address = fmt.Sprintf("%s:%s", args[1], args[2])
		creds, err = credentials.NewClientTLSFromFile(args[3], "")
	default:
		fmt.Println("USAGE: DocsMain [host port [trustCertCollectionFilePath]]")
		os.Exit(1)
	}
	if err != nil {
		log.Fatalf("failed to load credentials: %v", err)
	}

	// Create a SHA256 HMAC with the base64 decoded 'secret' string as its key
	secret, err := base64.StdEncoding.DecodeString("secret==")
	if err != nil {
		log.Fatalf("decoding error: %v \n", err)
	}
	hmacSha256 := hmac.New(sha256.New, secret)
	dummyJwt := "eyJraWQiOiI2YzY4OTIzMi03YTcxLTQ3NGItYjBlMi1lMmI1MzMyNDQzOWUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0In0.IL9QJQl55qn3oPsT7sFa7iwd5g1GsEQVr0IO7gCe1UmQdjT7jCIc-pUfjyYUgptPR8HBQl5ncXuBnxwjdXqOMwW1WhPmi_B3BRHQh3Sfu0zNXqKhkuz2-6DffXK1ek3DmK1NpaSikXtg2ruSQ4Uk5xHcnxmXY_SwEij0yot_JRKYEs-0RbyD5Z4jOFKcsbEW46WQmiWdgG3PUKiJT5TfdFd55JM55BwzSOdPIP1S_3dQ4VTDo30mWqAs1KaVbcPqCQmjT1PL0QScTp4w8-YPDcajcafIj98ve9LUoLBLraCIAX34D-hOxu643h9DoG2kIPFfZyXbkDTiUKOl7t-Ykg"

	opts := []grpc.DialOption{
		// Add an interceptor that signs messages with the provided secret.
		// Only messages to the private API that have a 'signed' field will be signed.
		grpc.WithUnaryInterceptor(auth.CreateMessageAuthInterceptor(hmacSha256)),
		// Add an interceptor that adds a JWT token when a request to a private service is made.
		grpc.WithPerRPCCredentials(auth.TulipAuth{
			Token: dummyJwt,
		}),
		grpc.WithTransportCredentials(creds),
	}

	// Set up a connection to the server.
	conn, err := grpc.Dial(address, opts...)
	if err != nil {
		log.Fatalf("could not connect: %v \n", err)
	}
	defer conn.Close()

	// Create a parent context that can be used in the requests
	mainContext := context.Background()

	go privateActiveOrdersServiceGetActiveOrders(conn, mainContext)
	go privateActiveOrdersServiceStreamActiveOrders(conn, mainContext)
	go privateOrderServiceCreateOrder(conn, mainContext)
	go privateOrderServiceCancelOrder(conn, mainContext)
	go privateOrderServiceGetEventsForOrder(conn, mainContext)
	go privateOrderServiceGetOrderEvents(conn, mainContext)
	go privateOrderServiceStreamOrderEvents(conn, mainContext)
	go privateTradeServiceGetTrades(conn, mainContext)
	go privateTradeServiceStreamTrades(conn, mainContext)
	go privateWalletServiceGetBalance(conn, mainContext)
	go privateWalletServiceStreamBalance(conn, mainContext)
	go publicMarketDetailServiceGetMarketDetails(conn, mainContext)
	go publicMarketDetailServiceStreamMarketDetails(conn, mainContext)
	go publicOrderbookServiceGetOrderbook(conn, mainContext)
	go publicOrderbookServiceStreamOrderbook(conn, mainContext)
	go publicTickerServiceGetTickers(conn, mainContext)
	go publicTickerServiceStreamTickers(conn, mainContext)
	go publicTradeServiceGetTrades(conn, mainContext)
	go publicTradeServiceStreamTrades(conn, mainContext)
	go publicOhlcServiceGetOhlcData(conn, mainContext)
	go publicOhlcServiceStreamOhlcData(conn, mainContext)

	// Wait until cancel
	fmt.Scanln()
}
