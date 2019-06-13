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

import nl.tulipsolutions.api.common.Market
import nl.tulipsolutions.api.common.Options
import nl.tulipsolutions.api.common.toEpochNanos
import nl.tulipsolutions.api.pub.GetPublicTradesRequest
import nl.tulipsolutions.api.pub.PublicTrade
import nl.tulipsolutions.api.pub.PublicTrades
import nl.tulipsolutions.api.pub.ReactorPublicTradeServiceGrpc
import nl.tulipsolutions.api.pub.StreamPublicTradesRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.time.Duration
import java.time.Instant

private val PUBLIC_TRADES_REQUEST_LIMIT_DEFAULT = GetPublicTradesRequest.getDescriptor()
    .findFieldByNumber(GetPublicTradesRequest.LIMIT_FIELD_NUMBER)
    .options
    .getExtension(Options.defaultLimit)!!

class MockPublicTradeService : ReactorPublicTradeServiceGrpc.PublicTradeServiceImplBase() {

    private fun createTradesGenerator(market: Market): Flux<PublicTrade> {
        return Flux.generate<PublicTrade, Long>(
            { 1L },
            { nextId, synchronousSink: SynchronousSink<PublicTrade> ->
                val price = getRandomPrice(25.0, 75.0)
                val baseAmount = Math.random() * 1000.0
                val quoteAmount = price * baseAmount
                synchronousSink.next(
                    PublicTrade.newBuilder()
                        .setMarket(market)
                        .setEventId(nextId)
                        .setPrice(price)
                        .setBaseAmount(baseAmount)
                        .setQuoteAmount(quoteAmount)
                        .setSide(getRandomSide())
                        .setTimestampNs(Instant.now().toEpochNanos())
                        .build()
                )
                nextId + 1
            }
        )
            .limitRate(1)
    }

    override fun getTrades(request: Mono<GetPublicTradesRequest>): Mono<PublicTrades> {
        return request
            .flatMap { r ->
                val limit = if (r.limit == 0) PUBLIC_TRADES_REQUEST_LIMIT_DEFAULT else r.limit
                createTradesGenerator(r.market)
                    .take(limit.toLong())
                    .collectList()
                    .map { trades -> PublicTrades.newBuilder().addAllTrades(trades).build() }
            }
    }

    override fun streamTrades(request: Mono<StreamPublicTradesRequest>): Flux<PublicTrade> {
        return request
            .flatMapMany { r -> createTradesGenerator(r.market) }
            .delayElements(Duration.ofMillis(1000))
    }
}
