package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemEndpointsTestKit
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._
import app.softnetwork.session.handlers.{JwtClaimsRefreshTokenDao, SessionRefreshTokenDao}
import app.softnetwork.session.model.SessionDataCompanion
import com.softwaremill.session.RefreshTokenStorage
import org.softnetwork.session.model.{JwtClaims, Session}

package Endpoints {

  package OneOff {
    package Cookie {

      class ResourceToLocalFileSystemEndpointsWithOneOffCookieBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with OneOffCookieSessionEndpointsTestKit[Session]
          with ResourceToLocalFileSystemEndpointsTestKit[Session]
          with CsrfCheckHeader
          with BasicSessionMaterials[Session] {

        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemEndpointsWithOneOffCookieJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with OneOffCookieSessionEndpointsTestKit[JwtClaims]
          with ResourceToLocalFileSystemEndpointsTestKit[JwtClaims]
          with CsrfCheckHeader
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }

    }

    package Header {

      class ResourceToLocalFileSystemEndpointsWithOneOffHeaderBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with OneOffHeaderSessionEndpointsTestKit[Session]
          with ResourceToLocalFileSystemEndpointsTestKit[Session]
          with CsrfCheckHeader
          with BasicSessionMaterials[Session] {

        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemEndpointsWithOneOffHeaderJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with OneOffHeaderSessionEndpointsTestKit[JwtClaims]
          with ResourceToLocalFileSystemEndpointsTestKit[JwtClaims]
          with CsrfCheckHeader
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }

    }

  }

  package Refreshable {
    package Cookie {

      class ResourceToLocalFileSystemEndpointsWithRefreshableCookieBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with RefreshableCookieSessionEndpointsTestKit[Session]
          with ResourceToLocalFileSystemEndpointsTestKit[Session]
          with CsrfCheckHeader
          with BasicSessionMaterials[Session] {

        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemEndpointsWithRefreshableCookieJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with RefreshableCookieSessionEndpointsTestKit[JwtClaims]
          with ResourceToLocalFileSystemEndpointsTestKit[JwtClaims]
          with CsrfCheckHeader
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }

    }

    package Header {

      class ResourceToLocalFileSystemEndpointsWithRefreshableHeaderBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with RefreshableHeaderSessionEndpointsTestKit[Session]
          with ResourceToLocalFileSystemEndpointsTestKit[Session]
          with CsrfCheckHeader
          with BasicSessionMaterials[Session] {

        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemEndpointsWithRefreshableHeaderJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with RefreshableHeaderSessionEndpointsTestKit[JwtClaims]
          with ResourceToLocalFileSystemEndpointsTestKit[JwtClaims]
          with CsrfCheckHeader
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }

    }

  }

}
