package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.query.{JdbcJournalProvider, JdbcOffsetProvider}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.persistence.query.{
  GenericResourceToExternalProcessorStream,
  ResourceToLocalFileSystemProcessorStream
}
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.model.{SessionData, SessionDataCompanion, SessionDataDecorator}
import com.typesafe.config.Config

trait ResourceToLocalFileSystemApi[SD <: SessionData with SessionDataDecorator[SD]]
    extends ResourceApi[SD] { self: SchemaProvider with CsrfCheck =>

  implicit def companion: SessionDataCompanion[SD]

  override def resourceToExternalProcessorStream
    : ActorSystem[_] => GenericResourceToExternalProcessorStream[Resource] =
    sys =>
      new ResourceToLocalFileSystemProcessorStream()
        with JdbcJournalProvider
        with JdbcOffsetProvider {
        override implicit def system: ActorSystem[_] = sys
        override def config: Config = ResourceToLocalFileSystemApi.this.config
      }
}
