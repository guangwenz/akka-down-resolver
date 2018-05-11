package akka.cluster

import akka.remote.testkit.{ MultiNodeSpec, MultiNodeSpecCallbacks }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

trait STMultiNodeSpec extends MultiNodeSpecCallbacks
  with WordSpecLike with Matchers with BeforeAndAfterAll {
  self: MultiNodeSpec =>
  override protected def beforeAll(): Unit = multiNodeSpecBeforeAll()

  override protected def afterAll(): Unit = multiNodeSpecAfterAll()

  override protected implicit def convertToWordSpecStringWrapper(s: String): WordSpecStringWrapper = new WordSpecStringWrapper(s"$s (on node '${self.myself.name}', $getClass)")
}
