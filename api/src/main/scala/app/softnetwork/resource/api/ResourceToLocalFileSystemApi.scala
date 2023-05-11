package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.query.{JdbcJournalProvider, JdbcOffsetProvider}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.persistence.query.{
  GenericResourceToExternalProcessorStream,
  ResourceToLocalFileSystemProcessorStream
}
import app.softnetwork.resource.service.{GenericResourceService, LocalFileSystemResourceService}
import com.typesafe.config.Config

trait ResourceToLocalFileSystemApi extends ResourceApi { _: SchemaProvider =>
  override def resourceToExternalProcessorStream
    : ActorSystem[_] => GenericResourceToExternalProcessorStream[Resource] =
    sys =>
      new ResourceToLocalFileSystemProcessorStream()
        with JdbcJournalProvider
        with JdbcOffsetProvider {
        override implicit def system: ActorSystem[_] = sys
        override def config: Config = ResourceToLocalFileSystemApi.this.config
      }

  override def resourceService: ActorSystem[_] => GenericResourceService = sys =>
    LocalFileSystemResourceService(sys)
}
