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
import nl.tulipsolutions.api.common.toCurrencyPair
import nl.tulipsolutions.api.pub.GetMarketDetailsRequest
import nl.tulipsolutions.api.pub.MarketDetail
import nl.tulipsolutions.api.pub.MarketDetails
import nl.tulipsolutions.api.pub.MarketStatus
import nl.tulipsolutions.api.pub.ReactorPublicMarketDetailServiceGrpc
import nl.tulipsolutions.api.pub.StreamMarketDetailsRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

const val MIN_AMOUNT = 0.0
const val MAX_AMOUNT = 1.7014118346046922E16

val marketDetails = listOf(
    MarketDetail.newBuilder()
        .setMarket(Market.BTC_EUR)
        .setMarketStatus(MarketStatus.OPEN)
        .setPriceResolution(0.01)
        .setAmountResolution(0.00000001)
        .setMinimumBaseOrderAmount(MIN_AMOUNT)
        .setMaximumBaseOrderAmount(MAX_AMOUNT)
        .setMinimumQuoteOrderAmount(MIN_AMOUNT)
        .setMaximumQuoteOrderAmount(MAX_AMOUNT)
        .setBase(Market.BTC_EUR.toCurrencyPair().first)
        .setQuote(Market.BTC_EUR.toCurrencyPair().second)
        .build(),
    MarketDetail.newBuilder()
        .setMarket(Market.BTC_USD)
        .setMarketStatus(MarketStatus.OPEN)
        .setPriceResolution(0.01)
        .setAmountResolution(0.00000001)
        .setMinimumBaseOrderAmount(MIN_AMOUNT)
        .setMaximumBaseOrderAmount(MAX_AMOUNT)
        .setMinimumQuoteOrderAmount(MIN_AMOUNT)
        .setMaximumQuoteOrderAmount(MAX_AMOUNT)
        .setBase(Market.BTC_USD.toCurrencyPair().first)
        .setQuote(Market.BTC_USD.toCurrencyPair().second)
        .build()
)

class MockPublicMarketDetailService : ReactorPublicMarketDetailServiceGrpc.PublicMarketDetailServiceImplBase() {
    override fun getMarketDetails(request: Mono<GetMarketDetailsRequest>): Mono<MarketDetails> =
        Mono.just(MarketDetails.newBuilder().addAllMarketDetails(marketDetails).build())

    override fun streamMarketDetails(request: Mono<StreamMarketDetailsRequest>): Flux<MarketDetail> =
        Flux.concat(Flux.fromIterable(marketDetails), Flux.never())
}
