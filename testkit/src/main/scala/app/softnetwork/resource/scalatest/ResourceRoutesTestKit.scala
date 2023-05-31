package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import app.softnetwork.resource.service.ResourceService
import app.softnetwork.session.scalatest.SessionServiceRoutes
import org.scalatest.Suite

trait ResourceRoutesTestKit extends SessionServiceRoutes { _: Suite =>

  def resourceService: ActorSystem[_] => ResourceService

  override def apiRoutes(system: ActorSystem[_]): Route =
    sessionServiceRoute(system).route ~ resourceService(system).route

}
