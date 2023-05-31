package app.softnetwork.resource.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoints
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.resource.service.ResourceServiceEndpoints
import app.softnetwork.session.service.SessionEndpoints
import com.softwaremill.session.CsrfCheck
import sttp.capabilities.akka.AkkaStreams
import sttp.tapir.server.ServerEndpoint

import scala.concurrent.Future

trait ResourceEndpoints[Resource <: GenericResource]
    extends ApiEndpoints
    with ResourceGuardian[Resource] { _: SchemaProvider with CsrfCheck =>

  def sessionEndpoints: ActorSystem[_] => SessionEndpoints

  def resourceEndpoints: ActorSystem[_] => ResourceServiceEndpoints

  override def endpoints: ActorSystem[_] => List[ServerEndpoint[Any with AkkaStreams, Future]] =
    system => resourceEndpoints(system).endpoints
}
