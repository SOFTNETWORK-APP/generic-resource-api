package app.softnetwork.resource.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiRoute, ApiRoutes}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.resource.service.ResourceService
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}

trait ResourceRoutes[SD <: SessionData with SessionDataDecorator[
  SD
], Resource <: GenericResource]
    extends ApiRoutes {
  _: ResourceGuardian[Resource] with SchemaProvider =>

  def resourceService: ActorSystem[_] => ResourceService[SD]

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        resourceService(system)
      )

}
