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
import nl.tulipsolutions.api.common.Market
import nl.tulipsolutions.api.common.allMarkets
import nl.tulipsolutions.api.pub.GetTickersRequest
import nl.tulipsolutions.api.pub.ReactorPublicTickerServiceGrpc
import nl.tulipsolutions.api.pub.StreamTickersRequest
import nl.tulipsolutions.api.pub.Tick
import nl.tulipsolutions.api.pub.Tickers
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private fun getMarketsDefaultIfEmpty(markets: List<Market>) = if (markets.isEmpty()) allMarkets else markets

class MockPublicTickerService : ReactorPublicTickerServiceGrpc.PublicTickerServiceImplBase() {

    private fun createTicker(market: Market): Tick {
        val bestBuy = getRandomPrice(45.0, 50.0)
        val bestSell = getRandomPrice(50.0, 55.0)
        val midPrice = (bestBuy + bestSell) / 2
        val high = getRandomPrice(midPrice, 60.0)
        val low = getRandomPrice(40.0, midPrice)
        val open = getRandomPrice(low, high)
        val close = getRandomPrice(low, high)

        return Tick.newBuilder()
            .setMarket(market)
            .setMidPrice(midPrice)
            .setBestBuyPrice(bestBuy)
            .setBestSellPrice(bestSell)
            .setBestBuySize(Math.random() * 10)
            .setBestSellSize(Math.random() * 10)
            .setDailyOpen(open)
            .setDailyHigh(midPrice)
            .setDailyLow(low)
            .setDailyClose(close)
            .setDailyVolumeBase(Math.random() * 1E6)
            .setDailyVolumeQuote(Math.random() * 1E6)
            .build()!!
    }

    override fun getTickers(request: Mono<GetTickersRequest>): Mono<Tickers> =
        request.map { getTickersRequest ->
            val markets = getMarketsDefaultIfEmpty(getTickersRequest.marketsList)

            Tickers.newBuilder()
                .addAllTicks(markets.map { market -> createTicker(market) })
                .build()
        }

    override fun streamTickers(request: Mono<StreamTickersRequest>): Flux<Tick> =
        request.flatMapMany { streamTickersRequest ->
            val markets = getMarketsDefaultIfEmpty(streamTickersRequest.marketsList)
            val initialTickers = markets.map { market -> createTicker(market) }

            request.thenMany(
                Flux.concat(
                    Flux.fromIterable(initialTickers),
                    Flux.generate<Tick> { sink ->
                        val marketIndex = (Math.random() * markets.size).toInt()
                        val market = markets[marketIndex]
                        sink.next(createTicker(market))
                    }
                        .delayElements(Duration.ofMillis(1000))
                )
            )
        }
}
