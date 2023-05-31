package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemRoutesTestKit
import app.softnetwork.session.scalatest.OneOffHeaderSessionServiceTestKit

class ResourceToLocalFileSystemRoutesWithOneOffHeaderSessionSpec
    extends ResourceServiceSpec
    with OneOffHeaderSessionServiceTestKit
    with ResourceToLocalFileSystemRoutesTestKit
