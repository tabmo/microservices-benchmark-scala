package dataprovider.service

import com.twitter.finagle.http.{Request, Response}
import com.twitter.logging.LoggerFactory
import com.twitter.util.{Future, NonFatal}
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.libs.json._

import dataprovider.Service
import service.HttpClient

object WeatherService extends Service with OpenWeatherProtocol {

  val logger = LoggerFactory("weather")()

  val cityCountryPattern = """/weather/([^/]*)/([^/]*)$""".r //weather/:city/:country
  val cityPattern = """/weather/([^/])*""".r //weather/:city

  // Prepare request
  val mockHost = "localhost:8001"
  val requestWeather = Request(s"http://$mockHost/weather.json")
  requestWeather.host = mockHost
  val httpClient = HttpClient.client(mockHost)

  // Common responses
  val internalServerError = Response(HttpResponseStatus.INTERNAL_SERVER_ERROR)
  val notFoundError = Response(HttpResponseStatus.NOT_FOUND)
  case object NotFoundException extends RuntimeException()

  def fetchWeather(city: String, country: Option[String]): Future[OpenWeatherResult] = {
    httpClient(requestWeather).map { response =>
      Json.parse(response.content.array()).as[OpenWeatherResult]
    }
  }

  def generateResponse[T](status: HttpResponseStatus, payload: T)(implicit wt: Writes[T]): Response = {
    val json: JsValue = Json.toJson(payload)
    val rawContent: Array[Byte] = json.toString().getBytes("UTF-8")

    val response = Response()
    response.setStatus(status)
    response.setContentTypeJson()
    response.setContent(ChannelBuffers.copiedBuffer(rawContent))
    response
  }

  def apply(request: Request) = {
    val futureWeather = request.uri match {
      case cityCountryPattern(city, country) => fetchWeather(city, Some(country))
      case cityPattern(city) => fetchWeather(city, None)
      case _ => Future.exception(NotFoundException)
    }

    futureWeather.map {
      case data: OpenWeatherSuccess => generateResponse(HttpResponseStatus.OK, data)
      case error: OpenWeatherError => generateResponse(HttpResponseStatus.BAD_REQUEST, error)
    }.handle {
      case NotFoundException => notFoundError
      case NonFatal(ex) =>
        logger.error(ex, "Cannot fetch weather")
        internalServerError
    }
  }
}

trait OpenWeatherResult
case class OpenWeatherSuccess(code: Int, temp: Int) extends OpenWeatherResult
case class OpenWeatherError(message: String) extends OpenWeatherResult

trait OpenWeatherProtocol {

  implicit val openWeatherReader: Reads[OpenWeatherResult] = Reads {
    case json: JsObject =>
      val httpCode = (json \ "cod").asOpt[Int].orElse((json \ "cod").asOpt[String].map(_.toInt)).getOrElse(500)
      if (httpCode != 200) {
        JsSuccess(OpenWeatherError((json \ "message").as[String]))
      } else {
        (for {
          code <- ((json \ "weather")(0) \ "id").asOpt[BigDecimal]
          temp <- (json \ "main" \ "temp").asOpt[BigDecimal]
        } yield (code, temp)) match {
          case Some((code, temp)) => JsSuccess(OpenWeatherSuccess(code.toIntExact, temp.toInt))
          case _ => JsError("cannot parse weather object")
        }
      }
    case _ => JsError("cannot parse json")
  }

  implicit val openWeatherWriter: Writes[OpenWeatherResult] = Writes {
    case OpenWeatherSuccess(code, temp) => Json.obj("code" -> code, "temp" -> temp)
    case OpenWeatherError(message) => Json.obj("error" -> message)
  }
}
