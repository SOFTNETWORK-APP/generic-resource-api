package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.persistence.launch.PersistentEntity
import app.softnetwork.persistence.launch.PersistenceGuardian._
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.session.persistence.typed.SessionRefreshTokenBehavior
import app.softnetwork.session.scalatest.SessionTestKit
import org.scalatest.Suite

trait ResourceRouteTestKit[Resource <: GenericResource]
    extends SessionTestKit
    with ResourceTestKit[Resource] { _: Suite with ApiRoutes =>

  override def beforeAll(): Unit = {
    super.beforeAll()
    // pre load routes
    apiRoutes(typedSystem())
  }

  def resourceEntities: ActorSystem[_] => Seq[PersistentEntity[_, _, _, _]] = sys =>
    Seq(resourceEntity(sys)) :+ implicitly[PersistentEntity[_, _, _, _]](
      SessionRefreshTokenBehavior
    )

  override def entities: ActorSystem[_] => Seq[PersistentEntity[_, _, _, _]] = resourceEntities

}
