package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.resource.service.{
  LocalFileSystemResourceServiceEndpoints,
  ResourceServiceEndpoints
}
import com.softwaremill.session.CsrfCheck
import org.scalatest.Suite

trait ResourceToLocalFileSystemEndpointsTestKit
    extends ResourceToLocalFileSystemRouteTestKit
    with ResourceEndpointsTestKit {
  _: Suite with CsrfCheck =>

  def resourceServiceEndpoints: ActorSystem[_] => ResourceServiceEndpoints = system =>
    LocalFileSystemResourceServiceEndpoints(system, sessionEndpoints(system))

}
