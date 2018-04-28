package akka.cluster

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration._

import akka.actor.Address
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
import com.typesafe.config.{ Config, ConfigFactory }
import org.scalatest.Suite

import scala.concurrent.duration.FiniteDuration

object MultiNodeClusterSpec {

  def clusterConfigWithFailureDetectorPuppet: Config =
    ConfigFactory.parseString("akka.cluster.failure-detector.implementation-class = akka.cluster.FailureDetectorPuppet").
      withFallback(clusterConfig)

  def clusterConfig(failureDetectorPuppet: Boolean): Config =
    if (failureDetectorPuppet) clusterConfigWithFailureDetectorPuppet else clusterConfig

  def clusterConfig: Config = ConfigFactory.parseString(
    s"""
    akka.actor.provider = cluster
    akka.actor.warn-about-java-serializer-usage = off
    akka.cluster {
      jmx.enabled                         = off
      gossip-interval                     = 200 ms
      leader-actions-interval             = 200 ms
      unreachable-nodes-reaper-interval   = 500 ms
      periodic-tasks-initial-delay        = 300 ms
      publish-stats-interval              = 0 s # always, when it happens
      failure-detector.heartbeat-interval = 500 ms

      run-coordinated-shutdown-when-down = off
    }
    akka.loglevel = INFO
    akka.log-dead-letters = off
    akka.log-dead-letters-during-shutdown = off
    akka.remote {
      log-remote-lifecycle-events = off
    }
    akka.loggers = ["akka.testkit.TestEventListener"]
    akka.test {
      single-expect-default = 5 s
    }
    akka.coordinated-shutdown.terminate-actor-system = off
    """.stripMargin)
}

trait MultiNodeClusterSpec extends Suite with STMultiNodeSpec with ImplicitSender {
  self: MultiNodeSpec =>

  private val cachedAddresses = new ConcurrentHashMap[RoleName, Address]()

  override def initialParticipants: Int = roles.size

  /**
   * Get the cluster node to use.
   */
  def cluster: Cluster = Cluster(system)

  def clusterView: ClusterReadView = cluster.readView

  implicit def address(role: RoleName): Address = {
    cachedAddresses.get(role) match {
      case null ⇒
        val address = node(role).address
        cachedAddresses.put(role, address)
        address
      case address ⇒ address
    }
  }

  /**
   * Use this method for the initial startup of the cluster node.
   */
  def startClusterNode(): Unit = {
    if (clusterView.members.isEmpty) {
      cluster join myself
      awaitAssert(clusterView.members.map(_.address) should contain(address(myself)))
    } else
      clusterView.self
  }

  /**
   * Initialize the cluster of the specified member
   * nodes (roles) and wait until all joined and `Up`.
   * First node will be started first  and others will join
   * the first.
   */
  def awaitClusterUp(roles: RoleName*): Unit = {
    runOn(roles.head) {
      // make sure that the node-to-join is started before other join
      startClusterNode()
    }
    enterBarrier(roles.head.name + "-started")
    if (roles.tail.contains(myself)) {
      cluster.join(roles.head)
    }
    if (roles.contains(myself)) {
      awaitMembersUp(numberOfMembers = roles.length)
    }
    enterBarrier(roles.map(_.name).mkString("-") + "-joined")
  }

  /**
   * Wait until the expected number of members has status Up has been reached.
   * Also asserts that nodes in the 'canNotBePartOfMemberRing' are *not* part of the cluster ring.
   */
  def awaitMembersUp(
    numberOfMembers: Int,
    canNotBePartOfMemberRing: Set[Address] = Set.empty,
    timeout: FiniteDuration = 25.seconds): Unit = {
    within(timeout) {
      if (canNotBePartOfMemberRing.nonEmpty) // don't run this on an empty set
        awaitAssert(canNotBePartOfMemberRing foreach (a ⇒ clusterView.members.map(_.address) should not contain a))
      awaitAssert(clusterView.members.size should ===(numberOfMembers))
      awaitAssert(clusterView.members.map(_.status) should ===(Set(MemberStatus.Up)))
      // clusterView.leader is updated by LeaderChanged, await that to be updated also
      val expectedLeader = clusterView.members.collectFirst {
        case m if m.dataCenter == cluster.settings.SelfDataCenter ⇒ m.address
      }
      awaitAssert(clusterView.leader should ===(expectedLeader))
    }
  }

  def assertMembersUp(address: Address*): Unit = address.foreach { addr =>
    awaitCond(cluster.state.members.exists(member => member.address == addr && member.status == MemberStatus.Up))
  }

  def assertMembersUnreachable(address: Address*): Unit = address.foreach { addr =>
    awaitCond(cluster.state.unreachable.exists(_.address == addr))
  }
}