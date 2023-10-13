package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemEndpointsTestKit
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.scalatest.OneOffHeaderSessionEndpointsTestKit

class ResourceToLocalFileSystemEndpointsWithOneOffHeaderSessionSpec
    extends ResourceServiceSpec
    with OneOffHeaderSessionEndpointsTestKit
    with ResourceToLocalFileSystemEndpointsTestKit
    with CsrfCheckHeader {}
