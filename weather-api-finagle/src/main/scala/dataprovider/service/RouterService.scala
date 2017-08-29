package dataprovider.service

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Response, Request}

object RouterService extends Service[Request, Response] {
  private val weatherPattern = """/weather/.*""".r

  def apply(request: Request) = request.getUri() match {
    case weatherPattern() => WeatherService(request)
    case _ => WeatherService(request)
  }
}
