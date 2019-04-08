import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object Main extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val uri = ConfigFactory.load().getString("external.domain")

  def responseFuture(i: Int): HttpResponse = {
    println(s"Making request number $i")
    val request = Http().singleRequest(HttpRequest(uri = uri))
    Await.result(request, Duration.Inf)
  }

  val source = Source(1 to 200000)
  val graph = source.via(Flow[Int].map(responseFuture)).to(Sink.ignore)

  graph.run()
}
