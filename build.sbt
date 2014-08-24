name := "toolbox-salat-example"

version := "0.0.1-SNAPSHOT"

organization := "com.julianpeeters"

scalaVersion := "2.10.3"
//scalaVersion := "2.11.2"

//resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

//resolvers += "spray" at "http://repo.spray.io/"

//resolvers += Resolver.file("Local Ivy Repository", file("/home/julianpeeters/.ivy2/local/"))(Resolver.ivyStylePatterns)

//resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies ++= Seq( 
  // "com.novus" %% "salat" % "1.9.1", 
"org.scalamacros" %% "quasiquotes" % "2.0.1",
//  "io.spray" %%  "spray-json" % "1.2.5",
  //"com.julianpeeters" %% "asm-salat-example" % "0.1-SNAPSHOT",
 // "com.gensler" %% "scalavro" % "0.4.0",
  //"org.ow2.asm" % "asm-all" % "4.1",
  //"com.novus" %% "salat" % "1.9.3"
  "com.novus" %% "salat" % "1.9.8"
)
