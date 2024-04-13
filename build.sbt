import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "3.3.3"
ThisBuild / scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-feature")

lazy val front = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name                            := "jk-studio",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("example")))
    },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
    libraryDependencies ++= Seq(
      "org.soundsofscala" %%% "sounds-of-scala" % "0.1.0-SNAPSHOT",
      "org.http4s"        %%% "http4s-dom"      % "0.2.11",
      "org.http4s"        %%% "http4s-dsl"      % "0.23.26",
    ),
  )

lazy val extension = project
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    externalNpm := {
      val dir = baseDirectory.value
      sys.process.Process("yarn", dir).!
      dir
    },
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.CommonJSModule)
    },
    libraryDependencies ++= Seq(
      "org.http4s" %%% "http4s-ember-server" % "0.23.26",
      "org.http4s" %%% "http4s-dsl"          % "0.23.26",
    ),
  )

lazy val root = project
  .in(file("."))
  .aggregate(front, extension)
