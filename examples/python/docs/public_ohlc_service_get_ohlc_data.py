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

from tulipsolutions.api.common import orders_pb2
from tulipsolutions.api.pub import ohlc_pb2, ohlc_pb2_grpc


def public_ohlc_service_get_ohlc_data(channel):
    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-request
    stub = ohlc_pb2_grpc.PublicOhlcServiceStub(channel)

    # Create a request for the OhlcBins for specified market and intervals.
    request = ohlc_pb2.GetOhlcRequest(
        market=orders_pb2.BTC_EUR,
    )
    request.intervals.extend([ohlc_pb2.ONE_SECOND, ohlc_pb2.ONE_MINUTE, ohlc_pb2.FIVE_MINUTES])

    try:
        # Make the request synchronously with a 1s deadline
        response = stub.GetOhlcData(request, timeout=1)
        print(response)
    except grpc.RpcError as e:
        print("PublicOhlcService.GetOhlcData error: " + str(e), file=sys.stderr)
    # CODEINCLUDE-END-MARKER: ref-code-example-request

    # CODEINCLUDE-BEGIN-MARKER: ref-code-example-response
    result_string = "{}\n".format(type(response).__name__)
    for ohlcbin in response.bins:
        result_string += \
            ("\t{} time: {} {} open: {}, high: {} low: {} close: {} "
             "volume_base: {} volume_quote: {} nr_trades: {}\n").format(
                type(ohlcbin).__name__,
                ohlcbin.timestamp_ns,
                ohlc_pb2.Interval.Name(ohlcbin.interval),
                ohlcbin.open,
                ohlcbin.high,
                ohlcbin.low,
                ohlcbin.close,
                ohlcbin.volume_base,
                ohlcbin.volume_quote,
                ohlcbin.number_of_trades,
            )
    print(result_string)
    # CODEINCLUDE-END-MARKER: ref-code-example-response
