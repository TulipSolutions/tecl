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

import nl.tulipsolutions.api.common.toCurrencyPair
import nl.tulipsolutions.api.common.toEpochNanos
import nl.tulipsolutions.api.priv.GetPrivateTradesRequest
import nl.tulipsolutions.api.priv.PrivateTrade
import nl.tulipsolutions.api.priv.PrivateTrades
import nl.tulipsolutions.api.priv.ReactorPrivateTradeServiceGrpc
import nl.tulipsolutions.api.priv.StreamPrivateTradesRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.time.Duration
import java.time.Instant
import java.util.Random

class MockPrivateTradeService : ReactorPrivateTradeServiceGrpc.PrivateTradeServiceImplBase() {

    private fun createTradesGenerator(): Flux<PrivateTrade> {
        val random = Random()

        return Flux.generate<PrivateTrade, Pair<Long, Long>>(
            { Pair(1L, 1L) },
            { previousIds, synchronousSink: SynchronousSink<PrivateTrade> ->
                val market = getRandomMarket()
                val tradeId = previousIds.first + random.nextInt(100)
                val orderId = previousIds.second + random.nextInt(1000)
                val price = getRandomPrice(25.0, 75.0)
                val baseAmount = Math.random() * 1000.0
                val quoteAmount = price * baseAmount
                synchronousSink.next(

                    PrivateTrade.newBuilder()
                        .setMarket(market)
                        .setTradeId(tradeId)
                        .setOrderId(orderId)
                        .setPrice(price)
                        .setBaseAmount(baseAmount)
                        .setQuoteAmount(quoteAmount)
                        .setFee(baseAmount / 50)
                        .setFeeCurrency(market.toCurrencyPair().first)
                        .setSide(getRandomSide())
                        .setTimestampNs(Instant.now().toEpochNanos())
                        .build()
                )
                Pair(tradeId, orderId)
            }
        )
            .limitRate(1)
    }

    override fun streamTrades(request: Mono<StreamPrivateTradesRequest>): Flux<PrivateTrade> {
        return request
            .flatMapMany { createTradesGenerator() }
            .delayElements(Duration.ofMillis(2000))
    }

    override fun getTrades(request: Mono<GetPrivateTradesRequest>): Mono<PrivateTrades> {
        return request
            .flatMapMany { createTradesGenerator() }
            .take(1000)
            .collectList()
            .map { trades -> PrivateTrades.newBuilder().addAllTrades(trades).build() }
    }
}
