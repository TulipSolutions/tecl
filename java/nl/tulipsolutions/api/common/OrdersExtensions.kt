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

package nl.tulipsolutions.api.common

import java.time.Instant
import java.util.concurrent.TimeUnit

fun Market.toCurrencyPair(): Pair<Currency, Currency> =
    when (this) {
        Market.BTC_EUR -> Pair(Currency.BTC, Currency.EUR)
        Market.BTC_USD -> Pair(Currency.BTC, Currency.USD)
        Market.UNRECOGNIZED, Market.INVALID_MARKET -> throw RuntimeException("Unrecognized Market")
    }

val allMarkets = Market.values()
    .filter { m -> m != Market.UNRECOGNIZED && m != Market.INVALID_MARKET }

val allSides = Side.values()
    .filter { s -> s != Side.UNRECOGNIZED && s != Side.INVALID_SIDE }

val allCurrencies = Currency.values()
    .filter { c -> c != Currency.UNRECOGNIZED && c != Currency.INVALID_CURRENCY }

fun Instant.toEpochNanos() = TimeUnit.SECONDS.toNanos(this.epochSecond) + this.nano
