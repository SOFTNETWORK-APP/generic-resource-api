package app.softnetwork.resource.scalatest

import app.softnetwork.resource.launch.GenericResourceRoutes
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.session.scalatest.SessionTestKit
import org.scalatest.Suite

trait GenericResourceRouteTestKit[Resource <: GenericResource]
    extends SessionTestKit
    with GenericResourceTestKit[Resource]
    with GenericResourceRoutes[Resource] { _: Suite => }
