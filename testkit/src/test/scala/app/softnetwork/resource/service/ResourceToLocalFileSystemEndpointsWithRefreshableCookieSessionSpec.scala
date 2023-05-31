package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemEndpointsTestKit
import app.softnetwork.session.scalatest.RefreshableCookieSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class ResourceToLocalFileSystemEndpointsWithRefreshableCookieSessionSpec
    extends ResourceServiceSpec
    with RefreshableCookieSessionEndpointsTestKit
    with ResourceToLocalFileSystemEndpointsTestKit
    with CsrfCheckHeader {}
