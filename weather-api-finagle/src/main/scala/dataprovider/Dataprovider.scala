package dataprovider

import java.net.InetSocketAddress

import com.twitter.finagle
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http._
import configuration.ServerConfig
import dataprovider.service.{RouterService, WeatherService}

object Dataprovider extends ServerConfig {

  def main(args: Array[String]) {

    val services: finagle.Service[Request, Response] = RouterService

    val address: InetSocketAddress = new InetSocketAddress(SERVER_PORT)
    ServerBuilder()
      .codec(RichHttp[Request](Http().compressionLevel(9).decompressionEnabled(true)))
      .bindTo(address)
      .name("HttpServer")
      .maxConcurrentRequests(MAX_CONCURRENT_REQUEST)
      .build(services)
  }

}
