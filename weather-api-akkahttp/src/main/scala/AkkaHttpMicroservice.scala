import play.api.libs.json._
import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config._
import scala.concurrent.{ExecutionContextExecutor, Future}

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

trait MockOpenWeatherService extends OpenWeatherProtocol {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  val logger: LoggingAdapter

  // static json served by nginx
  private lazy val openWeatherConnection: Flow[HttpRequest, HttpResponse, Any] =
    Http().outgoingConnection("localhost", 8001)

  private def fetchOpenWeatherRequest(request: HttpRequest): Future[HttpResponse] =
    Source.single(request).via(openWeatherConnection).runWith(Sink.head)

  def fetchWeather(city: String, country: Option[String]): Future[OpenWeatherResult] = {
    val request = RequestBuilding.Get("/weather.json")
    fetchOpenWeatherRequest(request).flatMap { response =>
      response.status match {
        case OK =>
          Unmarshal(response.entity).to[OpenWeatherResult]
        case _ =>
          logger.error(s"Unable to contact OpenWeatherAPI (${response.status})")
          Future.successful(OpenWeatherError("Unable to contact OpenWeather Service"))
      }
    }
  }
}

trait OpenWeatherApp extends MockOpenWeatherService {
  val routes = {
    (get & path("weather" / Segment / Segment.?)) { case (city, country) =>
      complete {
        fetchWeather(city, country).map[ToResponseMarshallable] {
          case success: OpenWeatherSuccess => OK -> success
          case error: OpenWeatherError => BadRequest -> error
        }
      }
    }
  }
}

object AkkaHttpMicroservice extends App with OpenWeatherApp {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}


// I use the same json deserialization/serialization code in my others implementation
// So bad performance is not related to that
trait OpenWeatherResult
case class OpenWeatherSuccess(code: Int, temp: Int) extends OpenWeatherResult
case class OpenWeatherError(message: String) extends OpenWeatherResult

trait OpenWeatherProtocol extends PlayJsonSupport {
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
