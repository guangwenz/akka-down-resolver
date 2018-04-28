credentials ++= (for {
  username <- Option(System.getenv().get("SONATYPE_USERNAME"))
  password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
} yield Seq(Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password))).getOrElse(Seq())

sonatypeProfileName := "org.guangwenz"
pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray)
pomExtra in Global := {
  <url>https://github.com/zgwmike/akka-down-resolver</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:github.com/zgwmike/akka-down-resolver.git</connection>
      <developerConnection>scm:git:git@github.com/zgwmike/akka-down-resolver.git</developerConnection>
      <url>git@github.com/zgwmike/akka-down-resolver.git</url>
    </scm>
    <developers>
      <developer>
        <id>zgwmike</id>
        <name>Guangwen Zhou</name>
        <url>www.guangwenz.org</url>
      </developer>
    </developers>
}