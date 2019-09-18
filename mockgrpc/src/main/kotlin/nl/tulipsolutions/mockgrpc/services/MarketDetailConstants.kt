// Copyright 2019 Tulip Solutions B.V.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package nl.tulipsolutions.mockgrpc.services

import nl.tulipsolutions.api.common.Market

object MarketDetailConstants {
    const val MIN_BASE_ORDER_AMOUNT = 1.0
    const val MAX_BASE_ORDER_AMOUNT = 1000.0
    const val MIN_QUOTE_ORDER_AMOUNT = 1.0
    const val MAX_QUOTE_ORDER_AMOUNT = 1000000.0
    val ENABLED_MARKETS = listOf(Market.BTC_EUR)
    val CLOSED_MARKETS = listOf(Market.BTC_USD)
    const val PRICE_RESOLUTION_DIGITS = 2
    const val AMOUNT_RESOLUTION_DIGITS = 8
    val AMOUNT_PRECISION = Math.pow(10.0, AMOUNT_RESOLUTION_DIGITS * -1.0)
    val PRICE_PRECISION = Math.pow(10.0, PRICE_RESOLUTION_DIGITS * -1.0)
}
