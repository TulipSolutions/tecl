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

package nl.tulipsolutions.api.pub

import java.time.Duration
import java.time.temporal.ChronoUnit
import nl.tulipsolutions.api.common.Options

public val allIntervals: List<Interval> = Interval.values()
    .filter { m -> m != Interval.UNRECOGNIZED && m != Interval.INVALID_INTERVAL }

public val GET_OHLC_REQUEST_DEFAULT_LIMIT = GetOhlcRequest.getDescriptor()
    .findFieldByNumber(GetOhlcRequest.LIMIT_FIELD_NUMBER)
    .options
    .getExtension(Options.defaultLimit)

// Note, this is only correct if the default = max
public val OHLC_RETENTION_NUM_BINS = GET_OHLC_REQUEST_DEFAULT_LIMIT

public fun Interval.toDuration(): Duration = when (this) {
    Interval.ONE_SECOND -> Duration.of(1, this.chronoUnit())
    Interval.ONE_MINUTE -> Duration.of(1, this.chronoUnit())
    Interval.FIVE_MINUTES -> Duration.of(5, this.chronoUnit())
    Interval.FIFTEEN_MINUTES -> Duration.of(15, this.chronoUnit())
    Interval.ONE_HOUR -> Duration.of(1, this.chronoUnit())
    Interval.FOUR_HOURS -> Duration.of(4, this.chronoUnit())
    Interval.TWELVE_HOURS -> Duration.of(12, this.chronoUnit())
    Interval.ONE_DAY -> Duration.of(1, this.chronoUnit())
    Interval.THREE_DAYS -> Duration.of(3, this.chronoUnit())
    Interval.ONE_WEEK -> Duration.ofDays(7)
    Interval.ONE_MONTH -> Duration.ofDays(30)
    Interval.UNRECOGNIZED, Interval.INVALID_INTERVAL ->
        throw RuntimeException("Cannot calculate Duration for interval $this")
}

public fun Interval.chronoUnit(): ChronoUnit = when (this) {
    Interval.ONE_SECOND -> ChronoUnit.SECONDS
    Interval.ONE_MINUTE -> ChronoUnit.MINUTES
    Interval.FIVE_MINUTES -> ChronoUnit.MINUTES
    Interval.FIFTEEN_MINUTES -> ChronoUnit.MINUTES
    Interval.ONE_HOUR -> ChronoUnit.HOURS
    Interval.FOUR_HOURS -> ChronoUnit.HOURS
    Interval.TWELVE_HOURS -> ChronoUnit.HOURS
    Interval.ONE_DAY -> ChronoUnit.DAYS
    Interval.THREE_DAYS -> ChronoUnit.DAYS
    Interval.ONE_WEEK -> ChronoUnit.WEEKS
    Interval.ONE_MONTH -> ChronoUnit.MONTHS
    Interval.UNRECOGNIZED, Interval.INVALID_INTERVAL ->
        throw RuntimeException("Cannot calculate ChronoUnit for interval $this")
}
