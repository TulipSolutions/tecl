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

import java.time.Duration
import java.util.Random
import nl.tulipsolutions.api.common.Market
import nl.tulipsolutions.api.common.Side
import nl.tulipsolutions.api.priv.ActiveOrderStatus
import nl.tulipsolutions.api.priv.GetActiveOrdersRequest
import nl.tulipsolutions.api.priv.OrderbookSnapshot
import nl.tulipsolutions.api.priv.ReactorPrivateActiveOrdersServiceGrpc
import nl.tulipsolutions.api.priv.StreamActiveOrdersRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ActiveOrdersStore {
    private val random = Random()
    private val DESIRED_NUM_ACTIVE_ORDERS = 10
    private var orderId = 0L

    private val activeOrders = MutableList(DESIRED_NUM_ACTIVE_ORDERS) { generateNew() }

    private fun getRandomMarket(): Market {
        return when (random.nextInt(10)) {
            in 0..7 -> Market.BTC_EUR
            in 7..10 -> Market.BTC_USD
            else -> throw RuntimeException("Unreachable")
        }
    }

    private fun getRandomSide(): Side {
        return when (random.nextInt(10)) {
            in 0..5 -> Side.BUY
            in 5..10 -> Side.SELL
            else -> throw RuntimeException("Unreachable")
        }
    }

    private fun shouldGenerateNew(size: Int) =
        getRandomBoolean(if (size < DESIRED_NUM_ACTIVE_ORDERS) 0.70 else 0.30)

    private fun generateNew(): ActiveOrderStatus {
        orderId += random.nextInt(100)
        val activeOrderStatus = ActiveOrderStatus.newBuilder()
            .setMarket(getRandomMarket())
            .setOrderId(orderId)
            .setDeadlineNs(getRandomDeadline())
        val baseAmount = random.nextDouble() * 1000.0
        val remaining = random.nextDouble() * baseAmount
        activeOrderStatus.limitOrderBuilder
            .setPrice(random.nextDouble() * 10000.0)
            .setBaseAmount(baseAmount)
            .setBaseRemaining(remaining)
            .setSide(getRandomSide())

        return activeOrderStatus.build()
    }

    fun generateEvent(): ActiveOrderStatus {
        return if (shouldGenerateNew(activeOrders.size)) {
            val activeOrderStatus = generateNew()
            this.activeOrders.add(activeOrderStatus)

            return activeOrderStatus
        } else {
            val deleteIdx = random.nextInt(activeOrders.size)
            val activeOrderStatus = activeOrders.removeAt(deleteIdx)
            activeOrderStatus.toBuilder()
                .clearLimitOrder()
                .build()
        }
    }

    fun getActiveOrders() = this.activeOrders
}

class MockPrivateActiveOrdersService : ReactorPrivateActiveOrdersServiceGrpc.PrivateActiveOrdersServiceImplBase() {
    override fun streamActiveOrders(request: Mono<StreamActiveOrdersRequest>): Flux<ActiveOrderStatus> {
        val db = ActiveOrdersStore()
        return request.thenMany(
            Flux.concat(
                Flux.fromIterable(db.getActiveOrders()),
                Flux.generate<ActiveOrderStatus> { sink -> sink.next(db.generateEvent()) }
                    .delayElements(Duration.ofMillis(1000))
            )
        )
    }

    override fun getActiveOrders(request: Mono<GetActiveOrdersRequest>): Mono<OrderbookSnapshot> =
        Mono.just(
            OrderbookSnapshot.newBuilder()
                .addAllOrders(ActiveOrdersStore().getActiveOrders())
                .build()
        )
}
