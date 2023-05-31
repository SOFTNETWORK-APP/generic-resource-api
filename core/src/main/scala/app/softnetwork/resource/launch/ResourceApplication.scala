package app.softnetwork.resource.launch

import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.api.server.launch.Application
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.GenericResource

trait ResourceApplication[Resource <: GenericResource]
    extends Application
    with ApiRoutes
    with ResourceGuardian[Resource] { _: SchemaProvider => }
