package app.softnetwork.resource.api

import app.softnetwork.persistence.jdbc.query.PostgresSchemaProvider

object ResourceToLocalFileSystemPostgresLauncher
    extends ResourceToLocalFileSystemApi
    with PostgresSchemaProvider
