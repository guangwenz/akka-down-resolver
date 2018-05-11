import Dependencies._
import com.typesafe.sbt.SbtPgp.autoImportImpl.useGpg
import sbt.Keys.{developers, licenses}

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.guangwenz",
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
  .aggregate(docs)

jvmOptions in MultiJvm := Seq("-Xmx256M")


lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  test := {},
  publishArtifact := false
)
lazy val micrositeSettings = Seq(
  micrositeName := "Akka Split Brain Resolver",
  micrositeDescription := "Akka Split Brain Resolver",
  micrositeAuthor := "Guangwen Zhou",
  //  micrositeHighlightTheme := "atom-one-light",
  micrositeHomepage := "https://github.com/guangwenz/akka-down-resolver",
  micrositeGithubOwner := "guangwenz",
  micrositeGithubRepo := "akka-down-resolver",
  //  micrositeExtraMdFiles := Map(file("CONTRIBUTING.md") -> microsites.ExtraMdFileConfig("contributing.md", "contributing")),
  micrositePalette := Map(
    "brand-primary" -> "#5B5988",
    "brand-secondary" -> "#292E53",
    "brand-tertiary" -> "#222749",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"),
  autoAPIMappings := true,
  ghpagesNoJekyll := false,
  fork in tut := true,
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
)
lazy val docs = (project in file("docs"))
  .settings(moduleName := "docs")
  .settings(micrositeSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(Seq(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    //    buildInfoPackage := "microsites"
  ): _*)
  .enablePlugins(MicrositesPlugin, GhpagesPlugin, BuildInfoPlugin)