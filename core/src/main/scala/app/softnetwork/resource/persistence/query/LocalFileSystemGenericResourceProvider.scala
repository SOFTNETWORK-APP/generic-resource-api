package app.softnetwork.resource.persistence.query

import app.softnetwork.persistence.ManifestWrapper
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.resource.spi.LocalFileSystemProvider

trait LocalFileSystemGenericResourceProvider[Resource <: GenericResource]
    extends ExternalGenericResourceProvider[Resource]
    with LocalFileSystemProvider { _: ManifestWrapper[Resource] => }
