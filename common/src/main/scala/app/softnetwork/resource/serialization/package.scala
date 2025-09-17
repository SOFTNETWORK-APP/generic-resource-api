package app.softnetwork.resource

import app.softnetwork.protobuf.ScalaPBSerializers.GeneratedEnumSerializer
import app.softnetwork.resource.model.Resource
import app.softnetwork.serialization.commonFormats
import org.json4s.Formats

package object serialization {

  val resourceFormats: Formats = commonFormats ++
    Seq(
      GeneratedEnumSerializer(Resource.ProviderType.enumCompanion)
    )

}
