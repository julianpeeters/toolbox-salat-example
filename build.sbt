name := "toolbox-salat-example"

version := "0.0.1-SNAPSHOT"

organization := "com.julianpeeters"

scalaVersion := "2.10.3"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies ++= Seq( 
  "org.scalamacros" %% "quasiquotes" % "2.0.1",
  "com.novus" %% "salat" % "1.9.8"
)
