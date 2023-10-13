package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{
  LocalFileSystemResourceServiceEndpoints,
  ResourceServiceEndpoints
}
import app.softnetwork.session.CsrfCheck
import org.scalatest.Suite

trait ResourceToLocalFileSystemEndpointsTestKit
    extends ResourceToLocalFileSystemRouteTestKit
    with ResourceEndpointsTestKit[Resource] {
  _: Suite with CsrfCheck =>

  def resourceEndpoints: ActorSystem[_] => ResourceServiceEndpoints =
    system => LocalFileSystemResourceServiceEndpoints(system, sessionEndpoints(system))

}
