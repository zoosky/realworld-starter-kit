import com.typesafe.sbt.digest.Import._
import com.typesafe.sbt.gzip.Import._
import com.typesafe.sbt.jse.JsEngineImport.JsEngineKeys
import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import play.sbt.PlayImport.PlayKeys
import play.sbt.PlayImport.PlayKeys._
import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import webscalajs.WebScalaJS.autoImport.{devCommands, scalaJSPipeline, scalaJSProjects}
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._


import scalajsbundler.sbtplugin.WebScalaJSBundlerPlugin

object Server {
  private[this] val dependencies = {
    import Dependencies._
    Seq(
      Play.playFilters,Play.playWebjars,
      Authentication.silhouette, Authentication.hasher, Authentication.persistence, Authentication.crypto,
      SharedDependencies.macwire, DatabaseUtils.flyway, DatabaseUtils.scalalikeJDBC, DatabaseUtils.postgres,
      Play.scalajsScripts, Utils.ficus
    )
  }

  private[this] lazy val serverSettings = Shared.commonSettings ++ Seq(
    maintainer := "",
    description := "",

    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= dependencies,
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    // connect to the client project
    scalaJSProjects := Seq(Client.client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
//    routesGenerator := InjectedRoutesGenerator,
    externalizeResources := false,

    // Sbt-Web
    JsEngineKeys.engineType := JsEngineKeys.EngineType.Node,
    includeFilter in (Assets, LessKeys.less) := "*.less",
    excludeFilter in (Assets, LessKeys.less) := "_*.less",
    LessKeys.compress in Assets := true,
    webpackConfigFile := Some(baseDirectory.value / "webpack.config.js"),

    // Fat-Jar Assembly
    fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value),
    mainClass in assembly := Some(Shared.projectName)

    // Code Quality
    //    scapegoatIgnoredFiles := Seq(".*/Row.scala", ".*/Routes.scala", ".*/ReverseRoutes.scala", ".*/JavaScriptReverseRoutes.scala", ".*/*.template.scala")
  )

  lazy val server = (project in file(".") copy(id = Shared.projectId) )
    .enablePlugins(
      SbtWeb, play.sbt.PlayScala, WebScalaJSBundlerPlugin
    )
    .dependsOn(Shared.sharedJvm)
    .settings(serverSettings: _*)
    .settings(Packaging.settings: _*)

  //    Shared.withProjects(ret, Seq(Shared.sharedJvm, Utilities.metrics))

}