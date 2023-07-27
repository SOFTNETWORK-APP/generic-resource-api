package app.softnetwork.resource.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.{ApiRoute, ApiRoutes}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.resource.service.ResourceService
import app.softnetwork.session.service.SessionService

trait ResourceRoutes[Resource <: GenericResource]
    extends ApiRoutes
    with ResourceGuardian[Resource] { _: SchemaProvider =>

  def sessionService: ActorSystem[_] => SessionService

  def resourceService: ActorSystem[_] => ResourceService

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        resourceService(system)
      )

}
