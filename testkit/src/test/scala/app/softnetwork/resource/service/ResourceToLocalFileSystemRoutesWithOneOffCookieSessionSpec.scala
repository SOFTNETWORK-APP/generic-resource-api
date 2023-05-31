package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemRoutesTestKit
import app.softnetwork.session.scalatest.OneOffCookieSessionServiceTestKit

class ResourceToLocalFileSystemRoutesWithOneOffCookieSessionSpec
    extends ResourceServiceSpec
    with OneOffCookieSessionServiceTestKit
    with ResourceToLocalFileSystemRoutesTestKit
