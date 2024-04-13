import org.scalajs.linker.interface.ModuleSplitStyle

lazy val front = project
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name         := "jk-studio",
    scalaVersion := "3.4.1",
    scalacOptions ++= Seq("-encoding", "utf-8", "-deprecation", "-feature"),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("example")))
    },
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
    libraryDependencies ++= Seq(
      "org.soundsofscala" %%% "sounds-of-scala" % "0.1.0-SNAPSHOT"
    ),
  )

lazy val root = project
  .in(file("."))
  .aggregate(front)
