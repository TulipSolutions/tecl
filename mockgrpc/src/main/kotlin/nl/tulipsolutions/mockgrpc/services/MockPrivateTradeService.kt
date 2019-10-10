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
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset
import java.util.Random
import nl.tulipsolutions.api.common.Market
import nl.tulipsolutions.api.common.Options
import nl.tulipsolutions.api.common.SearchDirection
import nl.tulipsolutions.api.common.allMarkets
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

private val GET_PRIVATE_TRADES_REQUEST_DEFAULT_LIMIT = GetPrivateTradesRequest.getDescriptor()
    .findFieldByNumber(GetPrivateTradesRequest.LIMIT_FIELD_NUMBER)
    .options
    .getExtension(Options.defaultLimit)

internal val btcGenesisEpochNanos = LocalDateTime.of(2009, Month.JANUARY, 3, 18, 15, 5)
    .toInstant(ZoneOffset.UTC)
    .toEpochNanos()

fun SearchDirection.getStepOperation(): (Long, Int) -> Long = when (this) {
    SearchDirection.FORWARD -> { l: Long, r: Int -> l + r }
    SearchDirection.BACKWARD -> { l: Long, r: Int -> l - r }
    SearchDirection.UNRECOGNIZED, SearchDirection.INVALID_SEARCH_DIRECTION ->
        throw RuntimeException("Unable to create step operation for SearchDirection $this")
}

class MockPrivateTradeService : ReactorPrivateTradeServiceGrpc.PrivateTradeServiceImplBase() {

    private fun createTradesGenerator(
        markets: List<Market>,
        searchDirection: SearchDirection,
        startEventId: Long,
        startTimestampNs: Long
    ): Flux<PrivateTrade> {
        val random = Random()
        val stepOperation = searchDirection.getStepOperation()

        return Flux.generate<PrivateTrade, Triple<Long, Long, Long>>(
            { Triple(startEventId, startTimestampNs, startEventId) },
            { (prevEventId, prevTimestamp, prevOrderId), synchronousSink: SynchronousSink<PrivateTrade> ->
                val market = getRandomMarket(markets)
                val eventId = stepOperation(prevEventId, random.nextInt(100))
                val timestamp = Math.min(
                    stepOperation(prevTimestamp, random.nextInt(100)),
                    Instant.now().toEpochNanos()
                )
                val orderId = stepOperation(prevOrderId, random.nextInt(1000))
                val price = getRandomPrice(25.0, 75.0)
                val baseAmount = random.nextDouble() * 1000.0
                val quoteAmount = price * baseAmount

                synchronousSink.next(
                    PrivateTrade.newBuilder()
                        .setMarket(market)
                        .setEventId(eventId)
                        .setOrderId(orderId)
                        .setPrice(price)
                        .setBaseAmount(baseAmount)
                        .setQuoteAmount(quoteAmount)
                        .setFee(baseAmount / 50)
                        .setFeeCurrency(market.toCurrencyPair().first)
                        .setSide(getRandomSide())
                        .setTimestampNs(timestamp)
                        .build()
                )
                Triple(eventId, timestamp, orderId)
            }
        )
            .limitRate(1)
    }

    private fun getStartFromEventId(eventId: Long, searchDirection: SearchDirection): Pair<Long, Long> =
        when (searchDirection) {
            SearchDirection.FORWARD -> Pair(eventId, btcGenesisEpochNanos + Math.min(eventId / 1_000, 1_000_000))
            SearchDirection.BACKWARD -> Pair(btcGenesisEpochNanos, Instant.now().toEpochNanos())
            else -> throw RuntimeException("Unable to create start from SearchDirection $searchDirection")
        }

    private fun getStartFromTimestamp(timestamp: Long, searchDirection: SearchDirection): Pair<Long, Long> =
        when (searchDirection) {
            SearchDirection.FORWARD -> Pair(timestamp / 1000, timestamp)
            SearchDirection.BACKWARD -> Pair(timestamp, timestamp)
            SearchDirection.UNRECOGNIZED, SearchDirection.INVALID_SEARCH_DIRECTION ->
                throw RuntimeException("Unable to create start from SearchDirection $searchDirection")
        }

    private fun getStartFromStartNotSet(searchDirection: SearchDirection): Pair<Long, Long> {
        return when (searchDirection) {
            SearchDirection.FORWARD -> Pair(0, btcGenesisEpochNanos)
            SearchDirection.BACKWARD -> Pair(1_000_000_000, Instant.now().toEpochNanos())
            else -> throw RuntimeException("Unable to create start from SearchDirection $searchDirection")
        }
    }

    override fun streamTrades(requestMono: Mono<StreamPrivateTradesRequest>): Flux<PrivateTrade> {
        return requestMono
            .flatMapMany { request ->
                val markets = if (request.marketsCount > 0) request.marketsList else allMarkets
                val (startId, startTimestampNs) = when (request.startCase) {
                    StreamPrivateTradesRequest.StartCase.EVENT_ID ->
                        getStartFromEventId(request.eventId, request.searchDirection)
                    StreamPrivateTradesRequest.StartCase.TIMESTAMP_NS ->
                        getStartFromTimestamp(request.timestampNs, request.searchDirection)
                    StreamPrivateTradesRequest.StartCase.START_NOT_SET, null ->
                        getStartFromStartNotSet(request.searchDirection)
                }

                createTradesGenerator(markets, request.searchDirection, startId, startTimestampNs)
            }
            .delayElements(Duration.ofSeconds(5))
    }

    override fun getTrades(requestMono: Mono<GetPrivateTradesRequest>): Mono<PrivateTrades> {
        return requestMono
            .flatMapMany { request ->
                val markets = if (request.marketsCount > 0) request.marketsList else allMarkets
                val (startId, startTimestampNs) = when (request.startCase) {
                    GetPrivateTradesRequest.StartCase.EVENT_ID ->
                        getStartFromTimestamp(request.eventId, request.searchDirection)
                    GetPrivateTradesRequest.StartCase.TIMESTAMP_NS ->
                        getStartFromTimestamp(request.timestampNs, request.searchDirection)
                    GetPrivateTradesRequest.StartCase.START_NOT_SET, null ->
                        getStartFromStartNotSet(request.searchDirection)
                }
                val limit = if (request.limit > 0) request.limit else GET_PRIVATE_TRADES_REQUEST_DEFAULT_LIMIT

                createTradesGenerator(markets, request.searchDirection, startId, startTimestampNs)
                    .take(limit.toLong())
            }
            .collectList()
            .map { trades -> PrivateTrades.newBuilder().addAllTrades(trades).build() }
    }
}
