package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.resource.service.{LocalFileSystemResourceService, ResourceService}
import org.scalatest.Suite

trait ResourceToLocalFileSystemRoutesTestKit
    extends ResourceToLocalFileSystemRouteTestKit
    with ResourceRoutesTestKit {
  _: Suite =>

  override def resourceService: ActorSystem[_] => ResourceService = system =>
    LocalFileSystemResourceService(system, sessionService(system))

}
