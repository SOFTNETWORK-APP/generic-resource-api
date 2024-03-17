package app.softnetwork.resource.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiEndpoints, Endpoint}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.resource.service.ResourceServiceEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}

trait ResourceEndpoints[SD <: SessionData with SessionDataDecorator[
  SD
], Resource <: GenericResource]
    extends ApiEndpoints {
  _: ResourceGuardian[Resource] with SchemaProvider with CsrfCheck =>

  def resourceEndpoints: ActorSystem[_] => ResourceServiceEndpoints[SD]

  override def endpoints: ActorSystem[_] => List[Endpoint] =
    system =>
      List(
        resourceEndpoints(system)
      )
}
