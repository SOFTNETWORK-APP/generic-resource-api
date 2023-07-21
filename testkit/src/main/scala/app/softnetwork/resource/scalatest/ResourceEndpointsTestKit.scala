package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.resource.service.ResourceServiceEndpoints
import app.softnetwork.session.scalatest.SessionEndpointsRoutes
import com.softwaremill.session.CsrfCheck

trait ResourceEndpointsTestKit extends SessionEndpointsRoutes { _: CsrfCheck =>

  def resourceServiceEndpoints: ActorSystem[_] => ResourceServiceEndpoints

  override def endpoints: ActorSystem[_] => List[ApiEndpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        resourceServiceEndpoints(system)
      )
}
