import Dependencies._
import com.typesafe.sbt.SbtPgp.autoImportImpl.useGpg
import sbt.Keys.{developers, licenses}

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.guangwenz",
      scalaVersion := "2.12.4",
      version := "1.2.4"
    )),
    crossScalaVersions := Seq("2.11.12", "2.12.6"),
    name := "akka-down-resolver",
    libraryDependencies += akkaCluster,
    libraryDependencies ++= akkaClusterTest,
    libraryDependencies += scalaTest % Test,

    //Publish settings
    credentials ++= (for {
      username <- Option(System.getenv().get("SONATYPE_USERNAME"))
      password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
    } yield Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password))).getOrElse(Seq()),
    publishMavenStyle := true,
    publishTo := sonatypePublishTo.value,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/zgwmike/akka-down-resolver"),
        "git@github.com:zgwmike/akka-down-resolver.git"
      )
    ),

    //PGP settings
    pgpPassphrase := (if (System.getenv().containsKey("PGP_PASSPHRASE")) Some(System.getenv().get("PGP_PASSPHRASE").toCharArray) else None),
    useGpg := false,
    pgpPublicRing := baseDirectory.value / "project" / ".gnupg" / "pubring.gpg",
    pgpSecretRing := baseDirectory.value / "project" / ".gnupg" / "secring.gpg",

    //SCM
    homepage := Some(url("https://github.com/zgwmike/akka-down-resolver")),
    developers := List(
      Developer(
        "zgwmike",
        "Guangwen Zhou",
        "zgwmike@hotmail.com",
        url("https://github.com/zgwmike")
      )
    ),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
  )
  .enablePlugins(MultiJvmPlugin)
  .configs(MultiJvm)

jvmOptions in MultiJvm := Seq("-Xmx256M")