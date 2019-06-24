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

package nl.tulipsolutions.mockgrpc.services

import nl.tulipsolutions.api.priv.CancelOrderRequest
import nl.tulipsolutions.api.priv.CancelOrderResponse
import nl.tulipsolutions.api.priv.CreateOrderRequest
import nl.tulipsolutions.api.priv.CreateOrderResponse
import nl.tulipsolutions.api.priv.LimitOrderResponse
import nl.tulipsolutions.api.priv.ReactorPrivateOrderServiceGrpc
import reactor.core.publisher.Mono

class MockPrivateOrderService : ReactorPrivateOrderServiceGrpc.PrivateOrderServiceImplBase() {
    override fun createOrder(request: Mono<CreateOrderRequest>): Mono<CreateOrderResponse> {
        return request
            .map { createOrderRequest ->
                CreateOrderResponse.newBuilder()
                    .setMarket(createOrderRequest.market)
                    .setLimitOrder(
                        LimitOrderResponse.newBuilder()
                            .setSide(createOrderRequest.limitOrder.side)
                            .setBaseAmount(createOrderRequest.limitOrder.baseAmount)
                            .setPrice(createOrderRequest.limitOrder.price)
                            .build()
                    )
                    .setOrderId(createOrderRequest.tonce)
                    .build()
            }
    }

    override fun cancelOrder(request: Mono<CancelOrderRequest>): Mono<CancelOrderResponse> {
        return request
            .map { cancelOrderRequest ->
                CancelOrderResponse.newBuilder()
                    .setMarket(cancelOrderRequest.market)
                    .setOrderId(cancelOrderRequest.orderId)
                    .build()
            }
    }
}
