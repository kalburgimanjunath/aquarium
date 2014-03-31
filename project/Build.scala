import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "akvaario"
  val appVersion      = "1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0-SNAPSHOT",
    "ws.securesocial" %% "securesocial" % "master-SNAPSHOT",
    "be.objectify" %% "deadbolt-scala" % "2.2-RC2",
    "org.scalacheck" %% "scalacheck" % "1.11.1",
    "com.typesafe.play" %% "play-test" % "2.2.0",
    "org.mockito" % "mockito-all" % "1.9.5"
    )


  val main = play.Project(appName, appVersion, appDependencies, settings = ScoverageSbtPlugin.instrumentSettings).settings(
    // Add your own project settings here      
    resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/", scalacOptions += "-feature",
    resolvers += Resolver.sonatypeRepo("releases"),
    resolvers += Resolver.url("Objectify Play Repository", url("http://schaloner.github.com/releases/"))(Resolver.ivyStylePatterns),
    javaOptions ++= Seq("-Xmx512M", "-Xmx2048M", "-XX:MaxPermSize=2048M"),
    ScoverageSbtPlugin.ScoverageKeys.excludedPackages in ScoverageSbtPlugin.scoverage := "<empty>;controllers.plugin;securesocial.*;Reverse*;views.html"
    )

}
