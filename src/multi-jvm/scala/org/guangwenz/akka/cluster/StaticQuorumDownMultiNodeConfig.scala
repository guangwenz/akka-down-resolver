package org.guangwenz.akka.cluster

import akka.cluster.MultiNodeClusterSpec
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory

object StaticQuorumDownMultiNodeConfig extends MultiNodeConfig {
  val node1: RoleName = role("node1")
  val node2: RoleName = role("node2")
  val node3: RoleName = role("node3")

  commonConfig(ConfigFactory.parseString(
    """
      |akka.cluster.downing-provider-class = "org.guangwenz.akka.cluster.SplitBrainResolver"
      |guangwenz.cluster.split-brain-resolver.active-strategy=static-quorum
      |guangwenz.cluster.split-brain-resolver.stable-after=5s
      |guangwenz.cluster.split-brain-resolver.static-quorum.quorum-size=2
    """.stripMargin)
    .withFallback(debugConfig(on = false))
    .withFallback(MultiNodeClusterSpec.clusterConfig)
  )
  testTransport(on = true)
}
