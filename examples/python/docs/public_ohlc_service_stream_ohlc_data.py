# Copyright 2019 Tulip Solutions B.V.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from __future__ import print_function

import sys
import grpc

from tulipsolutions.api.pub import ohlc_pb2, ohlc_pb2_grpc
from tulipsolutions.api.common import orders_pb2


def public_ohlc_service_stream_ohlc_data(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = ohlc_pb2_grpc.PublicOhlcServiceStub(channel)

    # Create a request for the OhlcBins for specified market and intervals.
    request = ohlc_pb2.StreamOhlcRequest(
        market=orders_pb2.BTC_EUR,
    )
    request.intervals.extend([ohlc_pb2.ONE_SECOND, ohlc_pb2.ONE_MINUTE, ohlc_pb2.FIVE_MINUTES])

    try:
        # Make the request synchronously and iterate over the received candles
        for response in stub.StreamOhlcData(request):
            print(response)
            # CODEINCLUDE-END-MARKER: ref-code-example-request
            parse_and_print(response)
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    except grpc.RpcError as e:
        print("PublicOhlcService.StreamOhlcData error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request


def parse_and_print(response):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    print(
        "{} time: {} {} open: {}, high: {} low: {} close: {} volume_base: {} volume_quote: {} nr_trades: {}\n".format(
            type(response).__name__,
            response.timestamp_ns,
            ohlc_pb2.Interval.Name(response.interval),
            response.open,
            response.high,
            response.low,
            response.close,
            response.volume_base,
            response.volume_quote,
            response.number_of_trades,
        )
    )
    # CODEINCLUDE-END-MARKER: ref-code-example-response
