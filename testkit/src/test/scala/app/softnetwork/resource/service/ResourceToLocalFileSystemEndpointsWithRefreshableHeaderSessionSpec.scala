package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemEndpointsTestKit
import app.softnetwork.session.scalatest.RefreshableHeaderSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class ResourceToLocalFileSystemEndpointsWithRefreshableHeaderSessionSpec
    extends ResourceServiceSpec
    with RefreshableHeaderSessionEndpointsTestKit
    with ResourceToLocalFileSystemEndpointsTestKit
    with CsrfCheckHeader {}
