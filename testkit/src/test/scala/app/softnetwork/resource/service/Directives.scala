package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemRoutesTestKit
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._

package Directives {
  package OneOff {
    package Cookie {

      class ResourceToLocalFileSystemRoutesWithOneOffCookieBasicSessionSpec
          extends ResourceServiceSpec
          with OneOffCookieSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with BasicSessionMaterials

      class ResourceToLocalFileSystemRoutesWithOneOffCookieJwtSessionSpec
          extends ResourceServiceSpec
          with OneOffCookieSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with JwtSessionMaterials
    }

    package Header {

      class ResourceToLocalFileSystemRoutesWithOneOffHeaderBasicSessionSpec
          extends ResourceServiceSpec
          with OneOffHeaderSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with BasicSessionMaterials

      class ResourceToLocalFileSystemRoutesWithOneOffHeaderJwtSessionSpec
          extends ResourceServiceSpec
          with OneOffHeaderSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with JwtSessionMaterials
    }

  }

  package Refreshable {
    package Cookie {

      class ResourceToLocalFileSystemRoutesWithRefreshableCookieBasicSessionSpec
          extends ResourceServiceSpec
          with RefreshableCookieSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with BasicSessionMaterials

      class ResourceToLocalFileSystemRoutesWithRefreshableCookieJwtSessionSpec
          extends ResourceServiceSpec
          with RefreshableCookieSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with JwtSessionMaterials
    }

    package Header {

      class ResourceToLocalFileSystemRoutesWithRefreshableHeaderBasicSessionSpec
          extends ResourceServiceSpec
          with RefreshableHeaderSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with BasicSessionMaterials

      class ResourceToLocalFileSystemRoutesWithRefreshableHeaderJwtSessionSpec
          extends ResourceServiceSpec
          with RefreshableHeaderSessionServiceTestKit
          with ResourceToLocalFileSystemRoutesTestKit
          with JwtSessionMaterials
    }

  }

}
