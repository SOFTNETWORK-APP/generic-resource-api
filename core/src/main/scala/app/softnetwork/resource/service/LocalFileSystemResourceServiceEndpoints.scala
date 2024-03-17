package app.softnetwork.resource.service

import app.softnetwork.resource.handlers.ResourceHandler
import app.softnetwork.resource.spi.LocalFileSystemProvider
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}
import app.softnetwork.session.service.SessionMaterials

trait LocalFileSystemResourceServiceEndpoints[SD <: SessionData with SessionDataDecorator[SD]]
    extends ResourceServiceEndpoints[SD]
    with LocalFileSystemProvider
    with ResourceHandler { _: SessionMaterials[SD] => }
