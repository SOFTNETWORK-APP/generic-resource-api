package app.softnetwork.resource.scalatest

import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}
import app.softnetwork.session.scalatest.SessionTestKit
import app.softnetwork.session.service.SessionMaterials
import org.scalatest.Suite

trait ResourceRouteTestKit[SD <: SessionData with SessionDataDecorator[
  SD
], Resource <: GenericResource]
    extends SessionTestKit[SD]
    with ResourceTestKit[Resource] { _: Suite with ApiRoutes with SessionMaterials[SD] =>

  override def beforeAll(): Unit = {
    super.beforeAll()
    // pre load routes
    apiRoutes(typedSystem())
  }

}
