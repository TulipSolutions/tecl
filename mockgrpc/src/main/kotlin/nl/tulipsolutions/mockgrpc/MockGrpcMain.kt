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

package nl.tulipsolutions.mockgrpc

import io.envoyproxy.pgv.ReflectiveValidatorIndex
import io.envoyproxy.pgv.ValidatorIndex
import io.grpc.Server
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyServerBuilder
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import java.io.FileInputStream
import java.util.Base64
import java.util.logging.Logger
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess
import nl.tulipsolutions.mockgrpc.interceptors.JwtServerInterceptor
import nl.tulipsolutions.mockgrpc.interceptors.MessageAuthServerInterceptor
import nl.tulipsolutions.mockgrpc.interceptors.ValidatingServerInterceptor
import nl.tulipsolutions.mockgrpc.services.MockPrivateActiveOrdersService
import nl.tulipsolutions.mockgrpc.services.MockPrivateOrderService
import nl.tulipsolutions.mockgrpc.services.MockPrivateTradeService
import nl.tulipsolutions.mockgrpc.services.MockPrivateWalletService
import nl.tulipsolutions.mockgrpc.services.MockPublicMarketDetailService
import nl.tulipsolutions.mockgrpc.services.MockPublicOhlcService
import nl.tulipsolutions.mockgrpc.services.MockPublicOrderbookService
import nl.tulipsolutions.mockgrpc.services.MockPublicTickerService
import nl.tulipsolutions.mockgrpc.services.MockPublicTradeService

private val LOGGER = Logger.getLogger(MockGrpcMain::class.java.name)!!
private const val MOCK_JWT =
    ("eyJraWQiOiI2YzY4OTIzMi03YTcxLTQ3NGItYjBlMi1lMmI1MzMyNDQzOWUiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMj" +
        "M0In0.IL9QJQl55qn3oPsT7sFa7iwd5g1GsEQVr0IO7gCe1UmQdjT7jCIc-pUfjyYUgptPR8HBQl5ncXuBnxwjdXqOMwW1WhPmi_B3BRHQh3" +
        "Sfu0zNXqKhkuz2-6DffXK1ek3DmK1NpaSikXtg2ruSQ4Uk5xHcnxmXY_SwEij0yot_JRKYEs-0RbyD5Z4jOFKcsbEW46WQmiWdgG3PUKiJT5" +
        "TfdFd55JM55BwzSOdPIP1S_3dQ4VTDo30mWqAs1KaVbcPqCQmjT1PL0QScTp4w8-YPDcajcafIj98ve9LUoLBLraCIAX34D-hOxu643h9DoG" +
        "2kIPFfZyXbkDTiUKOl7t-Ykg")
private const val DEFAULT_CERT_CHAIN_FILE_PATH = "mockgrpc/src/main/resources/certs/localhost.crt"
private const val DEFAULT_PRIVATE_KEY_FILE_PATH = "mockgrpc/src/main/resources/certs/localhost.pk8"

class MockGrpcMain {

    fun start(port: Int, sslContext: SslContext): Server {
        val secret = Base64.getDecoder().decode("secret")
        val key = SecretKeySpec(secret, "HmacSHA256")

        val jwtInterceptor = JwtServerInterceptor(MOCK_JWT)
        val messageAuthServerInterceptor = MessageAuthServerInterceptor(key)
        val validatorIndex = ReflectiveValidatorIndex(ValidatorIndex.ALWAYS_INVALID)
        val validateInterceptor = ValidatingServerInterceptor(validatorIndex)

        // Start the gRPC server
        val server = NettyServerBuilder
            .forPort(port)
            .sslContext(sslContext)
            .intercept(validateInterceptor)
            .intercept(jwtInterceptor)
            .intercept(messageAuthServerInterceptor)
            .addService(MockPublicTradeService())
            .addService(MockPublicOrderbookService())
            .addService(MockPrivateActiveOrdersService())
            .addService(MockPrivateOrderService())
            .addService(MockPrivateWalletService())
            .addService(MockPrivateTradeService())
            .addService(MockPublicTickerService())
            .addService(MockPublicMarketDetailService())
            .addService(MockPublicOhlcService())
            .build()
            .start()

        LOGGER.info("Server started, listening on $port")

        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            LOGGER.info("shutting down gRPC server")
            server.shutdown()
            LOGGER.info("server shut down")
        }))
        return server
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val certChain: FileInputStream
            val privateKey: FileInputStream
            val port: Int

            when (args.size) {
                0 -> {
                    port = 10011
                    certChain = FileInputStream(DEFAULT_CERT_CHAIN_FILE_PATH)
                    privateKey = FileInputStream(DEFAULT_PRIVATE_KEY_FILE_PATH)
                }
                1 -> {
                    port = args[0].toInt()
                    certChain = FileInputStream(DEFAULT_CERT_CHAIN_FILE_PATH)
                    privateKey = FileInputStream(DEFAULT_PRIVATE_KEY_FILE_PATH)
                }
                3 -> {
                    port = args[0].toInt()
                    certChain = FileInputStream(args[1])
                    privateKey = FileInputStream(args[2])
                }
                else -> {
                    LOGGER.info("USAGE: MockGrpcMain [port [certChainFilePath, privateKeyFilePath]]")
                    exitProcess(0)
                }
            }

            val sslContext = GrpcSslContexts.configure(SslContextBuilder.forServer(certChain, privateKey)).build()

            MockGrpcMain().start(port, sslContext).awaitTermination()
        }
    }
}
