package app.softnetwork.resource.service

import app.softnetwork.resource.handlers.ResourceHandler
import app.softnetwork.resource.spi.LocalFileSystemProvider
import app.softnetwork.session.service.SessionMaterials

trait LocalFileSystemResourceServiceEndpoints
    extends ResourceServiceEndpoints
    with LocalFileSystemProvider
    with ResourceHandler { _: SessionMaterials => }
