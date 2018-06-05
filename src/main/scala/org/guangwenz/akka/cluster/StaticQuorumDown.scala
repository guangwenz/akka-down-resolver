package org.guangwenz.akka.cluster

import java.util.concurrent.TimeUnit

import akka.actor.{ Cancellable, FSM, Props, Scheduler }
import akka.cluster.ClusterEvent._
import akka.cluster.{ Cluster, Member, MemberStatus }
import com.typesafe.config.Config

import scala.collection.immutable.SortedSet
import scala.concurrent.duration.FiniteDuration

/**
 * Split brain resolve strategy based on quorum size.
 *
 * @see http://developer.lightbend.com/docs/akka-commercial-addons/current/split-brain-resolver.html
 */
object StaticQuorumDown {

  trait State

  case object WAITING extends State

  case object CLUSTER_UP extends State

  case class Data()

  case object QuorumCheck

  object StaticQuorumDowningSettings {
    def apply(config: Config): StaticQuorumDowningSettings = {
      val role = config.getString("guangwenz.cluster.split-brain-resolver.static-quorum.role")
      new StaticQuorumDowningSettings(
        quorumSize = config.getInt("guangwenz.cluster.split-brain-resolver.static-quorum.quorum-size"),
        role = if (role.isEmpty) None else Some(role),
        stableAfter = FiniteDuration(config.getDuration("guangwenz.cluster.split-brain-resolver.stable-after").toMillis, TimeUnit.MILLISECONDS))
    }
  }

  final case class StaticQuorumDowningSettings(quorumSize: Int, role: Option[String], stableAfter: FiniteDuration)

  def props(settings: StaticQuorumDown.StaticQuorumDowningSettings): Props = Props(classOf[StaticQuorumDown], settings)
}

class StaticQuorumDown(settings: StaticQuorumDown.StaticQuorumDowningSettings) extends FSM[StaticQuorumDown.State, StaticQuorumDown.Data] {

  var leader = false
  var check: Option[Cancellable] = None

  import context.dispatcher

  def scheduler: Scheduler = context.system.scheduler

  def cluster: Cluster = Cluster(context.system)

  def scheduleCheck(): Unit = {
    check.foreach(_.cancel())
    check = Some(scheduler.scheduleOnce(settings.stableAfter, self, StaticQuorumDown.QuorumCheck))
  }

  def unreachable: Set[Member] = cluster.state.unreachable

  def reachable: SortedSet[Member] = cluster.state.members.diff(cluster.state.unreachable).diff(cluster.state.members.filter(_.status == MemberStatus.WeaklyUp))

  /**
   * check cluster state and down unreachable nodes based on quorum size.
   */
  def resolve(): Unit = {
    val unreachableCnt = unreachable.size
    val reachableCnt = reachable.size
    //Check if we have to down reachable group
    if (unreachableCnt >= settings.quorumSize || reachableCnt < settings.quorumSize) {
      log.warning(
        "downing reachable group with {} nodes and {} unreachable nodes for cluster [state={}]",
        reachableCnt, unreachableCnt, cluster.state)
      reachable.map(_.address).foreach(cluster.down)
    } //down unreachable nodes if there are any
    else if (unreachable.nonEmpty) {
      val isLeader = cluster.state.leader.contains(cluster.selfAddress)
      val isLeaderUp = cluster.state.leader.exists(a => reachable.map(_.address).contains(a))
      if (isLeader) {
        log.warning("downing unreachable nodes [state={}]", cluster.state)
        unreachable.map(_.address).foreach(cluster.down)
      } else if (isLeaderUp) {
        log.info("this node is not leader, doing nothing. [state={}]", cluster.state)
      } else {
        log.warning("no leader exists! downing unreachable nodes. [state={}]", cluster.state)
        unreachable.map(_.address).foreach(cluster.down)
      }
    }
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
    super.postStop()
  }

  override def preStart(): Unit = {
    cluster.subscribe(self, InitialStateAsEvents, classOf[ClusterDomainEvent])
    super.preStart()
  }

  startWith(StaticQuorumDown.WAITING, StaticQuorumDown.Data())

  when(StaticQuorumDown.WAITING) {
    case Event(MemberUp(_), _) =>
      val upNodesSize = cluster.state.members.count(_.status == MemberStatus.Up)
      if (upNodesSize >= settings.quorumSize) {
        log.info("cluster is up and running with number of members {} >= minimum quorum size {}", upNodesSize, settings.quorumSize)
        goto(StaticQuorumDown.CLUSTER_UP)
      } else stay()
    case _ =>
      log.info("waiting for cluster up and reach to full convergence")
      stay()
  }

  when(StaticQuorumDown.CLUSTER_UP) {
    case Event(MemberUp(_), _) =>
      scheduleCheck()
      stay()
    case Event(MemberRemoved(_, _), _) =>
      scheduleCheck()
      stay()
    case Event(UnreachableMember(_), _) =>
      scheduleCheck()
      stay()
    case Event(StaticQuorumDown.QuorumCheck, _) =>
      resolve()
      stay()
  }
  whenUnhandled {
    case Event(evt, StaticQuorumDown.Data()) =>
      stay()
  }
}

