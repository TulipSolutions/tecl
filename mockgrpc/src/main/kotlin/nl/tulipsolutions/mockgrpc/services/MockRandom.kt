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
import nl.tulipsolutions.api.common.Market
import nl.tulipsolutions.api.common.allCurrencies
import nl.tulipsolutions.api.common.allMarkets
import nl.tulipsolutions.api.common.allSides
import nl.tulipsolutions.api.common.toEpochNanos
import java.time.Instant
import java.util.Random

private val random = Random()

fun getRandomSide() = allSides[random.nextInt(allSides.size)]
fun getRandomMarket(markets: List<Market> = allMarkets) = markets[random.nextInt(markets.size)]
fun getRandomCurrency(currencies: List<Currency> = allCurrencies) = currencies[random.nextInt(allCurrencies.size)]

private const val DEFAULT_MIN_PRICE = 40.0
private const val DEFAULT_MAX_PRICE = 60.0

fun getRandomPrice(min: Double = DEFAULT_MIN_PRICE, max: Double = DEFAULT_MAX_PRICE) =
    (random.nextDouble() * (max - min)) + min

fun getRandomBoolean(p: Double) = random.nextFloat() < p
fun getRandomDeadline() = when (getRandomBoolean(0.10)) {
    true -> Instant.now().toEpochNanos() + (random.nextFloat() * 1e9 * 60).toLong() // 60 seconds
    false -> 0L
}
