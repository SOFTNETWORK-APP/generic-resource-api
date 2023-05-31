package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemEndpointsTestKit
import app.softnetwork.session.scalatest.OneOffCookieSessionEndpointsTestKit
import com.softwaremill.session.CsrfCheckHeader

class ResourceToLocalFileSystemEndpointsWithOneOffCookieSessionSpec
    extends ResourceServiceSpec
    with OneOffCookieSessionEndpointsTestKit
    with ResourceToLocalFileSystemEndpointsTestKit
    with CsrfCheckHeader {}
