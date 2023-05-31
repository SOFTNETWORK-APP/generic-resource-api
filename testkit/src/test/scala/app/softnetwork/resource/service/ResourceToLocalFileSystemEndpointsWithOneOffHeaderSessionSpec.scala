package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemEndpointsTestKit
import app.softnetwork.session.scalatest.OneOffHeaderSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class ResourceToLocalFileSystemEndpointsWithOneOffHeaderSessionSpec
    extends ResourceServiceSpec
    with OneOffHeaderSessionEndpointsTestKit
    with ResourceToLocalFileSystemEndpointsTestKit
    with CsrfCheckHeader {}
