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
import nl.tulipsolutions.api.pub.GetMarketDetailsRequest
import nl.tulipsolutions.api.pub.MarketDetail
import nl.tulipsolutions.api.pub.MarketDetails
import nl.tulipsolutions.api.pub.MarketStatus
import nl.tulipsolutions.api.pub.ReactorPublicMarketDetailServiceGrpc
import nl.tulipsolutions.api.pub.StreamMarketDetailsRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class MockPublicMarketDetailService() : ReactorPublicMarketDetailServiceGrpc.PublicMarketDetailServiceImplBase() {
    private fun getConstantBasedDetails() =
        (MarketDetailConstants.ENABLED_MARKETS + MarketDetailConstants.CLOSED_MARKETS).map {
            MarketDetail.newBuilder()
                .setMarket(it)
                .setMarketStatus(if (it in MarketDetailConstants.ENABLED_MARKETS) MarketStatus.OPEN else MarketStatus.CLOSED)
                .setPriceResolution(MarketDetailConstants.PRICE_PRECISION)
                .setAmountResolution(MarketDetailConstants.AMOUNT_PRECISION)
                .setMinimumBaseOrderAmount(MarketDetailConstants.MIN_BASE_ORDER_AMOUNT)
                .setMaximumBaseOrderAmount(MarketDetailConstants.MAX_BASE_ORDER_AMOUNT)
                .setMinimumQuoteOrderAmount(MarketDetailConstants.MIN_QUOTE_ORDER_AMOUNT)
                .setMaximumQuoteOrderAmount(MarketDetailConstants.MAX_QUOTE_ORDER_AMOUNT)
                .setBase(it.toCurrencyPair().first)
                .setQuote(it.toCurrencyPair().second)
                .build()
        }.toList()

    override fun getMarketDetails(request: Mono<GetMarketDetailsRequest>): Mono<MarketDetails> =
        Mono.just(MarketDetails.newBuilder().addAllMarketDetails(getConstantBasedDetails()).build())

    override fun streamMarketDetails(request: Mono<StreamMarketDetailsRequest>): Flux<MarketDetail> =
        Flux.concat(Flux.fromIterable(getConstantBasedDetails()), Flux.never())
}
