package app.softnetwork.resource.service

import akka.actor.typed.ActorSystem
import app.softnetwork.resource.handlers.ResourceHandler
import app.softnetwork.resource.spi.LocalFileSystemProvider
import app.softnetwork.session.service.SessionEndpoints
import org.slf4j.{Logger, LoggerFactory}

trait LocalFileSystemResourceServiceEndpoints
    extends ResourceServiceEndpoints
    with LocalFileSystemProvider
    with ResourceHandler

object LocalFileSystemResourceServiceEndpoints {
  def apply(
    _system: ActorSystem[_],
    _sessionEndpoints: SessionEndpoints
  ): LocalFileSystemResourceServiceEndpoints =
    new LocalFileSystemResourceServiceEndpoints {
      override implicit def system: ActorSystem[_] = _system
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
      override def sessionEndpoints: SessionEndpoints = _sessionEndpoints
    }
}
