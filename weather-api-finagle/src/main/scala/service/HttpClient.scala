package service

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Http, RichHttp, Response, Request}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

import configuration.ServerConfig

object HttpClient extends ServerConfig {
  def client(hosts: String): Service[Request, Response] = {
    ClientBuilder()
      .codec(RichHttp[Request](Http().compressionLevel(9).decompressionEnabled(true)))
      .hosts(hosts)
      .failFast(false)
      .hostConnectionMaxWaiters(Int.MaxValue)
      .hostConnectionLimit(Int.MaxValue)
      .name("HttpClient")
      .build()
  }
}
