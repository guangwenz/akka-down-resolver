package akka.cluster

import java.util.concurrent.atomic.AtomicReference

import akka.event.EventStream
import akka.remote.FailureDetector
import com.typesafe.config.Config

class FailureDetectorPuppet(config: Config, ev: EventStream) extends FailureDetector {

  trait Status

  case object Up extends Status

  case object Down extends Status

  case object Unknown extends Status

  private val status: AtomicReference[Status] = new AtomicReference[Status](Unknown)

  override def isAvailable: Boolean = status.get match {
    case Unknown | Up => true
    case Down => false
  }

  override def isMonitoring: Boolean = status.get != Unknown

  override def heartbeat(): Unit = status.compareAndSet(Unknown, Up)
}