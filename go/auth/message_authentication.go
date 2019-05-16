// Copyright 2019 Tulipsolutions B.V.
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

package auth

import (
	"context"
	"github.com/golang/protobuf/proto"
	pb_message_auth "github.com/tulipsolutions/tecl/tulipsolutions/api/priv/message_authentication"
	pb_order "github.com/tulipsolutions/tecl/tulipsolutions/api/priv/order"
	"google.golang.org/grpc"
	"hash"
	"sync"
)

func sign(pb proto.Message, mac hash.Hash, lock *sync.Mutex) ([]byte, []byte, error) {
	lock.Lock()
	defer lock.Unlock()
	rawBytes, err := proto.Marshal(pb)
	if err != nil {
		return nil, nil, err
	}
	mac.Reset()
	_, err = mac.Write(rawBytes)
	if err != nil {
		return nil, nil, err
	}
	sigBytes := mac.Sum(nil)

	return rawBytes, sigBytes, nil
}

// Create an interceptor that adds a signature to outgoing messages with a 'signed' field
func CreateMessageAuthInterceptor(hmacSha256 hash.Hash) grpc.UnaryClientInterceptor {
	lock := &sync.Mutex{}

	return func(ctx context.Context, method string, req, reply interface{}, cc *grpc.ClientConn, invoker grpc.UnaryInvoker, opts ...grpc.CallOption) error {
		// Using "github.com/golang/protobuf/descriptor" it is possible to get to a descriptor.Message from req
		// However we still cannot use it to derive the descriptors since descriptor.ForMessage requires a concrete struct
		// Hence the switch case and individual handling of each authenticated message
		switch r := req.(type) {
		case *pb_order.CreateOrderRequest:
			rawBytes, sigBytes, err := sign(r, hmacSha256, lock)
			if err != nil {
				return err
			}
			signedRequest := pb_order.CreateOrderRequest{
				Signed: &pb_message_auth.Signed{
					Raw: rawBytes,
					Sig: sigBytes,
				},
			}
			return invoker(ctx, method, &signedRequest, reply, cc, opts...)
		case *pb_order.CancelOrderRequest:
			rawBytes, sigBytes, err := sign(r, hmacSha256, lock)
			if err != nil {
				return err
			}
			signedRequest := pb_order.CancelOrderRequest{
				Signed: &pb_message_auth.Signed{
					Raw: rawBytes,
					Sig: sigBytes,
				},
			}
			return invoker(ctx, method, &signedRequest, reply, cc, opts...)
		default:
			return invoker(ctx, method, req, reply, cc, opts...)
		}
	}
}
