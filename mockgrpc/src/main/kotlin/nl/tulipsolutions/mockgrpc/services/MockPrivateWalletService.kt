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

import nl.tulipsolutions.api.common.Currency
import nl.tulipsolutions.api.common.allCurrencies
import nl.tulipsolutions.api.priv.BalanceResponse
import nl.tulipsolutions.api.priv.BalanceSnapshot
import nl.tulipsolutions.api.priv.GetBalanceRequest
import nl.tulipsolutions.api.priv.ReactorPrivateWalletServiceGrpc
import nl.tulipsolutions.api.priv.StreamBalanceRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.time.Duration

// 1% chance that the returned balance is 0
private fun generateBalance(currency: Currency) = if (Math.random() < 0.01) {
    BalanceResponse.newBuilder()
        .setCurrency(currency)
        .setLockedAmount(0.0)
        .setTotalAmount(0.0)
        .build()
} else {
    val r1 = Math.random() * 10_000
    val r2 = Math.random() * 10_000

    BalanceResponse.newBuilder()
        .setCurrency(currency)
        .setLockedAmount(Math.min(r1, r2))
        .setTotalAmount(Math.max(r1, r2))
        .build()
}

private fun generateBalances() = allCurrencies.map { currency: Currency ->
    generateBalance(
        currency
    )
}

class MockPrivateWalletService : ReactorPrivateWalletServiceGrpc.PrivateWalletServiceImplBase() {

    override fun streamBalance(request: Mono<StreamBalanceRequest>): Flux<BalanceResponse> =
        request
            .flatMapMany {
                val historicBalances = Flux.fromIterable(generateBalances())
                val liveBalances = Flux.generate { sink: SynchronousSink<BalanceResponse> ->
                    val currency = getRandomCurrency()
                    val balance = generateBalance(currency)
                    sink.next(balance)
                }
                    .delayElements(Duration.ofMillis(1000L + (Math.random() * 1000).toLong()))

                Flux.concat(historicBalances, liveBalances)
            }

    override fun getBalance(request: Mono<GetBalanceRequest>): Mono<BalanceSnapshot> =
        Mono.just(BalanceSnapshot.newBuilder().addAllBalanceResponse(generateBalances()).build())
}
