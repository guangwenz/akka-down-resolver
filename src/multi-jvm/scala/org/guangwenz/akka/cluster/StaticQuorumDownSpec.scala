package org.guangwenz.akka.cluster

import akka.cluster.MultiNodeClusterSpec
import akka.remote.testkit.MultiNodeSpec
import akka.remote.transport.ThrottlerTransportAdapter.Direction

import scala.concurrent.duration._



abstract class StaticQuorumDownSpec extends MultiNodeSpec(StaticQuorumDownMultiNodeConfig)
  with MultiNodeClusterSpec {

  import StaticQuorumDownMultiNodeConfig._

  override def initialParticipants: Int = roles.size

  "StaticQuorumDown" must {

    "wait cluster up" in {
      awaitClusterUp(roles: _*)
    }

    "down second node" in within(30.seconds) {
      runOn(roles: _*) {
        assertMembersUp(roles.map(r => node(r).address): _*)
      }
      enterBarrier("all nodes are up and running")

      runOn(node1) {
        testConductor.blackhole(node1, node2, Direction.Both).await
        testConductor.blackhole(node3, node2, Direction.Both).await
      }
      enterBarrier("network partition")

      runOn(node1, node3) {
        assertMembersUp(address(node1), address(node3))
        assertMembersUnreachable(address(node2))
      }
      enterBarrier("node2 unreachable")

      runOn(node1) {
        awaitCond(cluster.state.unreachable.isEmpty)
        awaitCond(cluster.state.members.size == 2)
      }
      enterBarrier("node2 downed")
    }

    "down entire cluster" in within(30.seconds) {
      runOn(node1) {
        testConductor.blackhole(node1, node3, Direction.Both).await
      }
      enterBarrier("split brain for 2 nodes")

      runOn(node1) {
        assertMembersUnreachable(address(node3))
        testConductor.getNodes.await.isEmpty
      }
      enterBarrier("first and third downed")
    }
  }
}

class StaticQuorumDownMultiJvmNode1 extends StaticQuorumDownSpec

class StaticQuorumDownMultiJvmNode2 extends StaticQuorumDownSpec

class StaticQuorumDownMultiJvmNode3 extends StaticQuorumDownSpec