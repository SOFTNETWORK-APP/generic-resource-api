package app.softnetwork.resource.service

import app.softnetwork.resource.handlers.ResourceHandler
import app.softnetwork.resource.model.Resource.ProviderType
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}
import app.softnetwork.session.service.SessionMaterials

trait LocalFileSystemResourceServiceEndpoints[SD <: SessionData with SessionDataDecorator[SD]]
    extends ResourceServiceEndpoints[SD]
    with ResourceHandler { _: SessionMaterials[SD] =>
  override def providerType: ProviderType = ProviderType.LOCAL
}
