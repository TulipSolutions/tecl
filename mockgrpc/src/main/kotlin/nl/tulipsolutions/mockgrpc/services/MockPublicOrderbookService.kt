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

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.util.Random
import nl.tulipsolutions.api.common.Market
import nl.tulipsolutions.api.common.Options
import nl.tulipsolutions.api.common.Side
import nl.tulipsolutions.api.pub.GetOrderbookRequest
import nl.tulipsolutions.api.pub.Length
import nl.tulipsolutions.api.pub.OrderbookEntries
import nl.tulipsolutions.api.pub.OrderbookEntry
import nl.tulipsolutions.api.pub.Precision
import nl.tulipsolutions.api.pub.ReactorPublicOrderbookServiceGrpc
import nl.tulipsolutions.api.pub.StreamOrderbookRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class OrderbookStore(
    private val length: Int,
    private val numDecimals: Int
) {

    private val random = Random()
    private val buys = HashSet<OrderbookEntry>()
    private val sells = HashSet<OrderbookEntry>()
    private val minPrice = 25.0
    private val maxPrice = 75.0

    init {
        for (i in 0..length) {
            generateNewEntry()
        }
    }

    enum class OrderbookEvent {
        UPDATE,
        REMOVE,
        ADD,
    }

    private fun generateNewEntry(): OrderbookEntry {
        val side = if (buys.size < sells.size) {
            Side.BUY
        } else {
            Side.SELL
        }
        val price = when (side) {
            Side.BUY -> getRandomPrice(minPrice, 50.0)
            else -> getRandomPrice(50.0, maxPrice)
        }

        val priceLevel = BigDecimal.valueOf(price)
            .setScale(numDecimals, RoundingMode.HALF_UP)
            .toDouble()

        val entry = OrderbookEntry.newBuilder()
            .setPriceLevel(priceLevel)
            .setSide(side)
            .setAmount(random.nextDouble() * 10 + 0.001)
            .setOrdersAtPriceLevel(random.nextInt(99) + 1)
            .build()

        addEntry(entry)
        return entry
    }

    private fun addEntry(entry: OrderbookEntry) =
        when (entry.side) {
            Side.BUY -> buys.add(entry)
            Side.SELL -> sells.add(entry)
            Side.UNRECOGNIZED, Side.INVALID_SIDE, null -> throw RuntimeException()
        }

    private fun removeEntry(entry: OrderbookEntry) =
        when (entry.side) {
            Side.BUY -> buys.remove(entry)
            Side.SELL -> sells.remove(entry)
            Side.UNRECOGNIZED, Side.INVALID_SIDE, null -> throw RuntimeException()
        }

    private fun getRandomEntry(side: Side) =
        when (side) {
            Side.BUY -> buys.elementAt(random.nextInt(buys.size))
            Side.SELL -> sells.elementAt(random.nextInt(sells.size))
            Side.UNRECOGNIZED, Side.INVALID_SIDE -> throw RuntimeException()
        }

    private fun generateUpdatedEntry(): OrderbookEntry {
        val oldEntry = getRandomEntry(getRandomSide())

        val newEntry = oldEntry.toBuilder()
            .setAmount(random.nextDouble() * 10 + 0.001)
            .setOrdersAtPriceLevel(random.nextInt(99) + 1)
            .build()

        removeEntry(oldEntry)
        addEntry(newEntry)
        return newEntry
    }

    private fun generateRemovedEntry(): OrderbookEntry {
        val oldEntry = getRandomEntry(getRandomSide())
        removeEntry(oldEntry)

        return oldEntry.toBuilder()
            .setAmount(0.0)
            .setOrdersAtPriceLevel(0)
            .build()
    }

    private fun generateEvent(): OrderbookEvent =
        when {
            buys.size + sells.size < length -> OrderbookEvent.ADD
            else -> if (random.nextBoolean()) {
                OrderbookEvent.UPDATE
            } else {
                OrderbookEvent.REMOVE
            }
        }

    fun generateUpdate(): OrderbookEntry {
        return when (generateEvent()) {
            OrderbookEvent.ADD -> generateNewEntry()
            OrderbookEvent.UPDATE -> generateUpdatedEntry()
            OrderbookEvent.REMOVE -> generateRemovedEntry()
        }
    }

    fun getOrderbookEntries(): Iterable<OrderbookEntry> = this.buys + this.sells
}

private val NUM_ENTRIES_25 = Length.getDescriptor()
    .findValueByNumber(Length.NUM_ENTRIES_25.number)
    .options
    .getExtension(Options.numEntries)

private val NUM_ENTRIES_100 = Length.getDescriptor()
    .findValueByNumber(Length.NUM_ENTRIES_100.number)
    .options
    .getExtension(Options.numEntries)

fun Length.numEntries(): Int =
    when (this) {
        Length.NUM_ENTRIES_25 -> NUM_ENTRIES_25
        Length.NUM_ENTRIES_100 -> NUM_ENTRIES_100
        Length.INVALID_LENGTH, Length.UNRECOGNIZED -> throw RuntimeException("Cannot determine num entries for $this")
    }

private val P0_OPTIONS = Precision.getDescriptor().findValueByNumber(Precision.P0.number).options
private val P1_OPTIONS = Precision.getDescriptor().findValueByNumber(Precision.P1.number).options
private val P2_OPTIONS = Precision.getDescriptor().findValueByNumber(Precision.P2.number).options
private val P3_OPTIONS = Precision.getDescriptor().findValueByNumber(Precision.P3.number).options

fun Precision.numDecimals(market: Market): Int {
    val extension = when (market) {
        Market.BTC_USD -> Options.numDecimalsBtcUsd
        Market.BTC_EUR -> Options.numDecimalsBtcUsd
        Market.INVALID_MARKET, Market.UNRECOGNIZED ->
            throw RuntimeException("Cannot determine num decimals for market $market")
    }

    return when (this) {
        Precision.P0 -> P0_OPTIONS.getExtension(extension)
        Precision.P1 -> P1_OPTIONS.getExtension(extension)
        Precision.P2 -> P2_OPTIONS.getExtension(extension)
        Precision.P3 -> P3_OPTIONS.getExtension(extension)
        Precision.INVALID_PRECISION, Precision.UNRECOGNIZED ->
            throw RuntimeException("Cannot determine num decimals for $this")
    }
}

class MockPublicOrderbookService : ReactorPublicOrderbookServiceGrpc.PublicOrderbookServiceImplBase() {

    override fun getOrderbook(request: Mono<GetOrderbookRequest>): Mono<OrderbookEntries> =
        request.map { getOrderbookRequest ->
            val numEntries = getOrderbookRequest.length.numEntries()
            val numDecimals = getOrderbookRequest.precision.numDecimals(getOrderbookRequest.market)
            val store = OrderbookStore(numEntries, numDecimals)

            OrderbookEntries.newBuilder().addAllEntries(store.getOrderbookEntries()).build()
        }

    override fun streamOrderbook(request: Mono<StreamOrderbookRequest>): Flux<OrderbookEntry> =
        request.flatMapMany { streamOrderbookRequest ->
            val numEntries = streamOrderbookRequest.length.numEntries()
            val numDecimals = streamOrderbookRequest.precision.numDecimals(streamOrderbookRequest.market)
            val store = OrderbookStore(numEntries, numDecimals)

            Flux.concat(
                Flux.fromIterable(store.getOrderbookEntries()),
                Flux.generate<OrderbookEntry> { sink -> sink.next(store.generateUpdate()) }
                    .delayElements(Duration.ofMillis(1000))
            )
        }
}
