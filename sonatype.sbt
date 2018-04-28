//credentials ++= (for {
//  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
//  password <- Option()
//} yield Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password))).getOrElse(Seq())

pgpPassphrase := Some(System.getenv().get("PGP_PASSWORD").toCharArray)
credentials += Credentials("Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  System.getenv().get("SONATYPE_USERNAME"),
  System.getenv().get("SONATYPE_PASSWORD"))

// POM settings for Sonatype
homepage := Some(url("https://github.com/zgwmike/akka-down-resolver"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/zgwmike/akka-down-resolver"),
    "git@github.com:zgwmike/akka-down-resolver.git"
  )
)

developers := List(
  Developer(
    "zgwmike",
    "Guangwen Zhou",
    "zgwmike@hotmail.com",
    url("https://github.com/zgwmike")
  )
)
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
