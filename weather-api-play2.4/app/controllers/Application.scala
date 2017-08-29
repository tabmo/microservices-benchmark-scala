package controllers

import scala.concurrent.Future

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.ws.WS
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._

class Application extends Controller with OpenWeatherProtocol {
  def weather(city: String, country: Option[String]) = Action.async {
    WeatherService.fetch(city, country).map {
      case data: OpenWeatherSuccess => Ok(Json.toJson(data))
      case error: OpenWeatherError => BadRequest(Json.toJson(error))
    }
  }
}

object WeatherService extends OpenWeatherProtocol {
  val request = WS.url("http://localhost:8001/weather.json")
  def fetch(city: String, country: Option[String]): Future[OpenWeatherResult] = {
    request.get.map(_.json.as[OpenWeatherResult])
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