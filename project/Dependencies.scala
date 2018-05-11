import sbt._

object Dependencies {
  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % "2.5.12"
  lazy val akkaClusterTest = Seq(
    "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.5.12" % Test,
    "com.typesafe.akka" %% "akka-testkit" % "2.5.12" % Test
  )
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
