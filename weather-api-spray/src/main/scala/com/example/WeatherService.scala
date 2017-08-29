package com.example

import scala.concurrent.Future

import akka.actor.{Actor, ActorContext}

import spray.client.pipelining._
import spray.http._
import spray.httpx.PlayJsonSupport
import spray.routing._

class WeatherServiceActor extends Actor with WeatherService {
  def actorRefFactory: ActorContext = context
  def receive = runRoute(routing)
}

trait WeatherService extends HttpService with OpenWeatherProtocol {

  implicit val ec = actorRefFactory.dispatcher

  private val request = Get("http://localhost:8001/weather.json")
  private val fetchWeatherPipeline: HttpRequest => Future[OpenWeatherResult] = sendReceive ~> unmarshal[OpenWeatherResult]
  private def fetchWeather(city: String, country: Option[String]): Future[OpenWeatherResult] = fetchWeatherPipeline(request)

  val routing =
    (get & path("weather" / Segment / Segment.?)) { case (city, country) =>
      complete {
        fetchWeather(city, country).map {
          case data: OpenWeatherSuccess => StatusCodes.OK -> data
          case error: OpenWeatherError => StatusCodes.BadRequest -> error
        }
      }
    }
}

// I use the same json deserialization/serialization code in my others implementation
// So bad performance is not related to that
trait OpenWeatherResult
case class OpenWeatherSuccess(code: Int, temp: Int) extends OpenWeatherResult
case class OpenWeatherError(message: String) extends OpenWeatherResult

trait OpenWeatherProtocol extends PlayJsonSupport {

  import play.api.libs.json._

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
