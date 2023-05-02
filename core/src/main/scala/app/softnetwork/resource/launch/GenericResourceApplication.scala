package app.softnetwork.resource.launch

import app.softnetwork.api.server.launch.Application
import app.softnetwork.resource.model.GenericResource

trait GenericResourceApplication[Resource <: GenericResource]
    extends Application
    with GenericResourceRoutes[Resource]
