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

import nl.tulipsolutions.api.common.Options
import nl.tulipsolutions.api.common.toEpochNanos
import nl.tulipsolutions.api.pub.GetOhlcRequest
import nl.tulipsolutions.api.pub.GetOhlcResponse
import nl.tulipsolutions.api.pub.Interval
import nl.tulipsolutions.api.pub.OhlcBin
import nl.tulipsolutions.api.pub.ReactorPublicOhlcServiceGrpc
import nl.tulipsolutions.api.pub.StreamOhlcRequest
import nl.tulipsolutions.api.pub.allIntervals
import nl.tulipsolutions.api.pub.toDuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant

private val GET_OHLC_REQUEST_LIMIT_DEFAULT = GetOhlcRequest.getDescriptor()
    .findFieldByNumber(GetOhlcRequest.LIMIT_FIELD_NUMBER)
    .options
    .getExtension(Options.defaultLimit)!!

class MockPublicOhlcService : ReactorPublicOhlcServiceGrpc.PublicOhlcServiceImplBase() {

    private fun generateBin(interval: Interval, timestampNs: Long): OhlcBin {
        val high = getRandomPrice(50.0, 75.0)
        val low = getRandomPrice(25.0, 50.0)
        val avgPrice = getRandomPrice(low, high)
        val volumeBase = getRandomPrice(10.0, 100.0)

        return OhlcBin.newBuilder()
            .setInterval(interval)
            .setTimestampNs(timestampNs)
            .setOpen(getRandomPrice(low, high))
            .setHigh(high)
            .setLow(low)
            .setClose(getRandomPrice(low, high))
            .setVolumeBase(volumeBase)
            .setVolumeQuote(volumeBase * avgPrice)
            .setNumberOfTrades(getRandomPrice(0.0, 100.0).toInt())
            .build()
    }

    private fun generateHistoricBinsForInterval(interval: Interval, numBins: Int): List<OhlcBin> {
        val now = Instant.now().toEpochNanos()
        return ((numBins + 1) downTo 1).map { i -> generateBin(interval, now - (i * interval.toDuration().toNanos())) }
    }

    private fun generateHistoricBins(intervals: List<Interval>, numBins: Int): List<OhlcBin> =
        intervals.flatMap { interval -> generateHistoricBinsForInterval(interval, numBins) }

    override fun getOhlcData(request: Mono<GetOhlcRequest>): Mono<GetOhlcResponse> =
        request.map { getOhlcRequest ->
            val intervals = if (getOhlcRequest.intervalsList.isEmpty()) allIntervals else getOhlcRequest.intervalsList
            val numBins = if (getOhlcRequest.limit == 0) GET_OHLC_REQUEST_LIMIT_DEFAULT else getOhlcRequest.limit

            GetOhlcResponse.newBuilder()
                .addAllBins(generateHistoricBins(intervals, numBins))
                .build()
        }

    private fun generateFluxForInterval(interval: Interval): Flux<OhlcBin> {
        return Flux.generate<OhlcBin, Long>(
            { Instant.now().toEpochNanos() },
            { previousTs, sink ->
                val now = Instant.now().toEpochNanos()
                // Always generate a new value, but set the timestamp to the previous timestamp, unless now > timestamp + interval
                val ts = if (now > previousTs + interval.toDuration().toNanos()) {
                    now
                } else {
                    previousTs
                }
                sink.next(generateBin(interval, ts))
                ts
            }
        )
            // We cannot pre-generate items due to the timestamp being calculated at generate time
            .limitRate(1)
            // Generate a new value for each interval stream every second, regardless of interval
            .delayElements(Duration.ofMillis(1000))
    }

    override fun streamOhlcData(request: Mono<StreamOhlcRequest>): Flux<OhlcBin> =
        request.flatMapMany { streamOhlcRequest ->
            val intervals = if (streamOhlcRequest.intervalsList.isEmpty()) {
                allIntervals
            } else {
                streamOhlcRequest.intervalsList
            }
            val numHistoricBins = streamOhlcRequest.initialDepth

            Flux.concat(
                Flux.fromIterable(generateHistoricBins(intervals, numHistoricBins)),
                Flux.merge(*intervals.map { interval ->
                    generateFluxForInterval(interval)
                }.toTypedArray())
            )
        }
}
