package app.softnetwork.resource.service

import akka.actor.typed.ActorSystem
import app.softnetwork.resource.handlers.ResourceHandler
import app.softnetwork.resource.spi.LocalFileSystemProvider
import app.softnetwork.session.service.SessionService
import org.slf4j.{Logger, LoggerFactory}

trait LocalFileSystemResourceService
    extends ResourceService
    with LocalFileSystemProvider
    with ResourceHandler

object LocalFileSystemResourceService {
  def apply(
    aSystem: ActorSystem[_],
    aSessionService: SessionService
  ): LocalFileSystemResourceService =
    new LocalFileSystemResourceService {
      override implicit def system: ActorSystem[_] = aSystem
      override def sessionService: SessionService = aSessionService
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
    }
}
