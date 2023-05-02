package app.softnetwork.resource.persistence.query

import app.softnetwork.persistence.query.{JournalProvider, OffsetProvider}
import app.softnetwork.resource.model.Resource

trait ResourceToLocalFileSystemProcessorStream
    extends GenericResourceToExternalProcessorStream[Resource]
    with LocalFileSystemResourceProvider { _: JournalProvider with OffsetProvider =>
  override val externalProcessor: String = "localfilesystem"
  override protected val manifestWrapper: ManifestW = ManifestW()
}
