package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.resource.service.ResourceServiceEndpoints
import app.softnetwork.session.scalatest.SessionEndpointsRoutes
import com.softwaremill.session.CsrfCheck
import sttp.capabilities
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait ResourceEndpointsTestKit extends SessionEndpointsRoutes { _: CsrfCheck =>

  def resourceServiceEndpoints: ActorSystem[_] => ResourceServiceEndpoints

  override def endpoints
    : ActorSystem[_] => List[ServerEndpoint[AkkaStreams with capabilities.WebSockets, Future]] =
    system =>
      sessionServiceEndpoints(system).endpoints ++ resourceServiceEndpoints(system).endpoints
}
