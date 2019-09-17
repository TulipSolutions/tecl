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

package nl.tulipsolutions.grpccommon

import com.google.protobuf.Any
import com.google.protobuf.Message
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.protobuf.StatusProto

fun buildGrpcStatusRuntimeException(
    statusCode: Code,
    stringMessage: String,
    vararg detailMessages: Message
) =
    StatusProto.toStatusRuntimeException(
        Status.newBuilder()
            .setCode(statusCode.number)
            .setMessage(stringMessage)
            .addAllDetails(detailMessages.map { message -> Any.pack(message) })
            .build()
    )
