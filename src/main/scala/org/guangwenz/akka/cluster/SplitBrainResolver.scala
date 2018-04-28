package org.guangwenz.akka.cluster

import java.util.concurrent.TimeUnit

import akka.ConfigurationException
import akka.actor.{ ActorSystem, Props }
import akka.cluster.{ Cluster, DowningProvider }

import scala.concurrent.duration.FiniteDuration

final class SplitBrainResolver(system: ActorSystem) extends DowningProvider {
  private def config = Cluster(system).settings.config

  override def downRemovalMargin: FiniteDuration = FiniteDuration(config.getDuration("guangwenz.cluster.split-brain-resolver.stable-after").toMillis, TimeUnit.MILLISECONDS)

  override def downingActorProps: Option[Props] = {
    val strategy = config.getString("guangwenz.cluster.split-brain-resolver.active-strategy")
    strategy match {
      case "static-quorum" =>
        val settings = StaticQuorumDown.StaticQuorumDowningSettings(config)
        Some(StaticQuorumDown.props(settings))
      case _ =>
        throw new ConfigurationException("Unknown split brain resolve strategy, available options are static-quorum")
    }
  }
}