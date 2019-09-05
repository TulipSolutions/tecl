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

import nl.tulipsolutions.api.common.SearchDirection
import nl.tulipsolutions.api.common.Market
import nl.tulipsolutions.api.common.Options
import nl.tulipsolutions.api.common.allMarkets
import nl.tulipsolutions.api.common.toCurrencyPair
import nl.tulipsolutions.api.common.toEpochNanos
import nl.tulipsolutions.api.priv.CancelOrderEvent
import nl.tulipsolutions.api.priv.CancelOrderRequest
import nl.tulipsolutions.api.priv.CancelOrderResponse
import nl.tulipsolutions.api.priv.CreateLimitOrderEvent
import nl.tulipsolutions.api.priv.CreateOrderEvent
import nl.tulipsolutions.api.priv.CreateOrderRequest
import nl.tulipsolutions.api.priv.CreateOrderResponse
import nl.tulipsolutions.api.priv.FillOrderEvent
import nl.tulipsolutions.api.priv.GetEventsForOrderRequest
import nl.tulipsolutions.api.priv.GetOrderEventsRequest
import nl.tulipsolutions.api.priv.GetOrderEventsResponse
import nl.tulipsolutions.api.priv.LimitOrderResponse
import nl.tulipsolutions.api.priv.OrderEvent
import nl.tulipsolutions.api.priv.ReactorPrivateOrderServiceGrpc
import nl.tulipsolutions.api.priv.StreamOrderEventsRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.time.Duration
import java.time.Instant
import java.util.Random

private val GET_ORDER_EVENTS_REQUEST_DEFAULT_LIMIT = GetOrderEventsRequest.getDescriptor()
    .findFieldByNumber(GetOrderEventsRequest.LIMIT_FIELD_NUMBER)
    .options
    .getExtension(Options.defaultLimit)

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
                    .setDeadlineNs(createOrderRequest.deadlineNs)
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

    private fun createOrderEventsGenerator(
        markets: List<Market>,
        searchDirection: SearchDirection,
        startOrderId: Long,
        startTimestampNs: Long,
        startEventId: Long
    ): Flux<OrderEvent> {
        val random = Random()
        val stepOperation = searchDirection.getStepOperation()

        return Flux.generate<OrderEvent, Triple<Long, Long, Long>>(
            { Triple(startOrderId, startTimestampNs, startEventId) },
            { (prevOrderId, prevTimestamp, prevEventId), synchronousSink: SynchronousSink<OrderEvent> ->
                val market = getRandomMarket(markets)
                val orderId = stepOperation(prevOrderId, random.nextInt(1000))
                val timestamp = Math.min(
                    stepOperation(prevTimestamp, random.nextInt(100)),
                    Instant.now().toEpochNanos()
                )
                val eventId = stepOperation(prevEventId, random.nextInt(100))
                val price = getRandomPrice(25.0, 75.0)
                val baseAmount = random.nextDouble() * 1000.0
                val quoteAmount = price * baseAmount
                val orderEvent = OrderEvent.newBuilder()
                    .setOrderId(orderId)
                    .setMarket(market)
                    .setEventId(eventId)
                    .setTimestampNs(timestamp)

                when (random.nextInt(10)) {
                    in 0..4 -> orderEvent.setCreateOrderEvent(
                        CreateOrderEvent.newBuilder()
                            .setCreateLimitOrder(
                                CreateLimitOrderEvent.newBuilder()
                                    .setSide(getRandomSide())
                                    .setPrice(price)
                                    .setBaseAmount(baseAmount)
                            )
                            .setDeadlineNs(getRandomDeadline())
                    )
                    in 5..7 -> orderEvent.setCancelOrderEvent(CancelOrderEvent.newBuilder())
                    in 8..9 -> orderEvent.setFillOrderEvent(
                        FillOrderEvent.newBuilder()
                            .setPrice(price)
                            .setBaseAmount(baseAmount)
                            .setQuoteAmount(quoteAmount)
                            .setFee(quoteAmount * 0.002)
                            .setFeeCurrency(market.toCurrencyPair().second)
                            .setSide(getRandomSide())
                    )
                }

                synchronousSink.next(orderEvent.build())
                Triple(orderId, timestamp, eventId)
            }
        )
            .limitRate(1)
    }

    private val highStartEventId = 1_000_000_000L
    private val lowStartEventId = 1_000_000L

    private val maxTonceShiftNs = Duration.ofSeconds(5).toNanos()

    // Random timestamp between (orderId - maxTonceShiftNs, orderId + maxTonceShiftNs)
    private fun orderIdFromTimestamp(timestampNs: Long) =
        Math.max((timestampNs - maxTonceShiftNs) + (maxTonceShiftNs * 2 + Math.random()).toLong(), 0)

    private fun getStartFromOrderId(orderId: Long, searchDirection: SearchDirection): Triple<Long, Long, Long> =
        when (searchDirection) {
            SearchDirection.FORWARD -> Triple(orderId, orderIdFromTimestamp(orderId), lowStartEventId)
            SearchDirection.BACKWARD -> Triple(orderId, orderIdFromTimestamp(orderId), highStartEventId)
            else -> throw RuntimeException("Unable to create start from SearchDirection $searchDirection")
        }

    private fun getStartFromTimestamp(timestamp: Long, searchDirection: SearchDirection): Triple<Long, Long, Long> =
        when (searchDirection) {
            SearchDirection.FORWARD -> Triple(orderIdFromTimestamp(timestamp), timestamp, lowStartEventId)
            SearchDirection.BACKWARD -> Triple(orderIdFromTimestamp(timestamp), timestamp, highStartEventId)
            SearchDirection.UNRECOGNIZED, SearchDirection.INVALID_SEARCH_DIRECTION ->
                throw RuntimeException("Unable to create start from SearchDirection $searchDirection")
        }

    private fun getStartFromEventId(eventId: Long, searchDirection: SearchDirection): Triple<Long, Long, Long> {
        val nowNs = Instant.now().toEpochNanos()
        return when (searchDirection) {
            SearchDirection.FORWARD -> Triple(nowNs, nowNs, eventId)
            SearchDirection.BACKWARD -> Triple(nowNs, nowNs, eventId)
            else -> throw RuntimeException("Unable to create start from SearchDirection $searchDirection")
        }
    }

    private fun getStartFromStartNotSet(searchDirection: SearchDirection): Triple<Long, Long, Long> =
        when (searchDirection) {
            SearchDirection.FORWARD -> Triple(btcGenesisEpochNanos, btcGenesisEpochNanos, lowStartEventId)
            SearchDirection.BACKWARD -> Triple(btcGenesisEpochNanos, btcGenesisEpochNanos, highStartEventId)
            else -> throw RuntimeException("Unable to create start from SearchDirection $searchDirection")
        }

    override fun streamOrderEvents(requestMono: Mono<StreamOrderEventsRequest>): Flux<OrderEvent> {
        return requestMono
            .flatMapMany { request ->
                val markets = if (request.marketsCount > 0) request.marketsList else allMarkets

                val (startOrderId, startTimestampNs, startEventId) = when (request.startCase) {
                    StreamOrderEventsRequest.StartCase.ORDER_ID ->
                        getStartFromOrderId(request.orderId, request.searchDirection)
                    StreamOrderEventsRequest.StartCase.TIMESTAMP_NS ->
                        getStartFromTimestamp(request.timestampNs, request.searchDirection)
                    StreamOrderEventsRequest.StartCase.EVENT_ID, null ->
                        getStartFromEventId(request.eventId, request.searchDirection)
                    StreamOrderEventsRequest.StartCase.START_NOT_SET ->
                        getStartFromStartNotSet(request.searchDirection)
                }

                createOrderEventsGenerator(
                    markets,
                    request.searchDirection,
                    startOrderId,
                    startTimestampNs,
                    startEventId
                )
            }
            .delayElements(Duration.ofSeconds(2))
    }

    override fun getOrderEvents(requestMono: Mono<GetOrderEventsRequest>): Mono<GetOrderEventsResponse> {
        return requestMono
            .flatMapMany { request ->
                val markets = if (request.marketsCount > 0) request.marketsList else allMarkets

                val (startOrderId, startTimestampNs, startEventId) = when (request.startCase) {
                    GetOrderEventsRequest.StartCase.ORDER_ID ->
                        getStartFromOrderId(request.orderId, request.searchDirection)
                    GetOrderEventsRequest.StartCase.TIMESTAMP_NS ->
                        getStartFromTimestamp(request.timestampNs, request.searchDirection)
                    GetOrderEventsRequest.StartCase.EVENT_ID, null ->
                        getStartFromEventId(request.eventId, request.searchDirection)
                    GetOrderEventsRequest.StartCase.START_NOT_SET ->
                        getStartFromStartNotSet(request.searchDirection)
                }
                val limit = if (request.limit > 0) request.limit else GET_ORDER_EVENTS_REQUEST_DEFAULT_LIMIT

                createOrderEventsGenerator(
                    markets,
                    request.searchDirection,
                    startOrderId,
                    startTimestampNs,
                    startEventId
                )
                    .take(limit.toLong())
            }
            .collectList()
            .map { events -> GetOrderEventsResponse.newBuilder().addAllEvents(events).build() }
    }

    override fun getEventsForOrder(requestMono: Mono<GetEventsForOrderRequest>): Mono<GetOrderEventsResponse> {
        return requestMono
            .flatMapMany { request ->

                val (startOrderId, startTimestampNs, startEventId) = getStartFromOrderId(
                    request.orderId,
                    SearchDirection.FORWARD
                )

                val randomEvents = createOrderEventsGenerator(
                    listOf(request.market),
                    SearchDirection.FORWARD,
                    startOrderId,
                    startTimestampNs,
                    startEventId
                )

                Flux.merge(
                    randomEvents.takeUntil { orderEvent -> orderEvent.hasCreateOrderEvent() }.last(),
                    randomEvents.takeUntil { orderEvent -> orderEvent.hasFillOrderEvent() }.last(),
                    randomEvents.takeUntil { orderEvent -> orderEvent.hasFillOrderEvent() }.last(),
                    randomEvents.takeUntil { orderEvent -> orderEvent.hasCancelOrderEvent() }.last()
                )
                    .map { orderEvent -> orderEvent.toBuilder().setOrderId(request.orderId).build() }
            }
            .collectList()
            .map { events -> GetOrderEventsResponse.newBuilder().addAllEvents(events).build() }
    }
}
