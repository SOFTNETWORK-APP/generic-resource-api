package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemEndpointsTestKit
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._

package Endpoints {

  package OneOff {
    package Cookie {

      class ResourceToLocalFileSystemEndpointsWithOneOffCookieBasicSessionSpec
          extends ResourceServiceSpec
          with OneOffCookieSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with BasicSessionMaterials

      class ResourceToLocalFileSystemEndpointsWithOneOffCookieJwtSessionSpec
          extends ResourceServiceSpec
          with OneOffCookieSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with JwtSessionMaterials
    }

    package Header {

      class ResourceToLocalFileSystemEndpointsWithOneOffHeaderBasicSessionSpec
          extends ResourceServiceSpec
          with OneOffHeaderSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with BasicSessionMaterials

      class ResourceToLocalFileSystemEndpointsWithOneOffHeaderJwtSessionSpec
          extends ResourceServiceSpec
          with OneOffHeaderSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with JwtSessionMaterials
    }

  }

  package Refreshable {
    package Cookie {

      class ResourceToLocalFileSystemEndpointsWithRefreshableCookieBasicSessionSpec
          extends ResourceServiceSpec
          with RefreshableCookieSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with BasicSessionMaterials

      class ResourceToLocalFileSystemEndpointsWithRefreshableCookieJwtSessionSpec
          extends ResourceServiceSpec
          with RefreshableCookieSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with JwtSessionMaterials
    }

    package Header {

      class ResourceToLocalFileSystemEndpointsWithRefreshableHeaderBasicSessionSpec
          extends ResourceServiceSpec
          with RefreshableHeaderSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with BasicSessionMaterials

      class ResourceToLocalFileSystemEndpointsWithRefreshableHeaderJwtSessionSpec
          extends ResourceServiceSpec
          with RefreshableHeaderSessionEndpointsTestKit
          with ResourceToLocalFileSystemEndpointsTestKit
          with CsrfCheckHeader
          with JwtSessionMaterials
    }

  }

}
