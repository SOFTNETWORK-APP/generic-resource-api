package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemRoutesTestKit
import app.softnetwork.session.scalatest._
import app.softnetwork.session.service._
import app.softnetwork.session.handlers.{JwtClaimsRefreshTokenDao, SessionRefreshTokenDao}
import app.softnetwork.session.model.SessionDataCompanion
import com.softwaremill.session.RefreshTokenStorage
import org.softnetwork.session.model.{JwtClaims, Session}

package Directives {
  package OneOff {
    package Cookie {

      class ResourceToLocalFileSystemRoutesWithOneOffCookieBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with OneOffCookieSessionServiceTestKit[Session]
          with ResourceToLocalFileSystemRoutesTestKit[Session]
          with BasicSessionMaterials[Session] {

        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemRoutesWithOneOffCookieJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with OneOffCookieSessionServiceTestKit[JwtClaims]
          with ResourceToLocalFileSystemRoutesTestKit[JwtClaims]
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }
    }

    package Header {

      class ResourceToLocalFileSystemRoutesWithOneOffHeaderBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with OneOffHeaderSessionServiceTestKit[Session]
          with ResourceToLocalFileSystemRoutesTestKit[Session]
          with BasicSessionMaterials[Session] {
        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemRoutesWithOneOffHeaderJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with OneOffHeaderSessionServiceTestKit[JwtClaims]
          with ResourceToLocalFileSystemRoutesTestKit[JwtClaims]
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }

    }

  }

  package Refreshable {
    package Cookie {

      class ResourceToLocalFileSystemRoutesWithRefreshableCookieBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with RefreshableCookieSessionServiceTestKit[Session]
          with ResourceToLocalFileSystemRoutesTestKit[Session]
          with BasicSessionMaterials[Session] {
        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemRoutesWithRefreshableCookieJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with RefreshableCookieSessionServiceTestKit[JwtClaims]
          with ResourceToLocalFileSystemRoutesTestKit[JwtClaims]
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }

    }

    package Header {

      class ResourceToLocalFileSystemRoutesWithRefreshableHeaderBasicSessionSpec
          extends ResourceServiceSpec[Session]
          with RefreshableHeaderSessionServiceTestKit[Session]
          with ResourceToLocalFileSystemRoutesTestKit[Session]
          with BasicSessionMaterials[Session] {
        override implicit def refreshTokenStorage: RefreshTokenStorage[Session] =
          SessionRefreshTokenDao(ts)

        override implicit def companion: SessionDataCompanion[Session] = Session
      }

      class ResourceToLocalFileSystemRoutesWithRefreshableHeaderJwtSessionSpec
          extends ResourceServiceSpec[JwtClaims]
          with RefreshableHeaderSessionServiceTestKit[JwtClaims]
          with ResourceToLocalFileSystemRoutesTestKit[JwtClaims]
          with JwtSessionMaterials[JwtClaims] {
        override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

        override implicit def refreshTokenStorage: RefreshTokenStorage[JwtClaims] =
          JwtClaimsRefreshTokenDao(ts)
      }

    }

  }

}
