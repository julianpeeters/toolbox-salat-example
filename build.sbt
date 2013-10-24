name := "toolbox-salat-example"

version := "0.0.1-SNAPSHOT"

organization := "com.julianpeeters"

scalaVersion := "2.10.1"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "spray" at "http://repo.spray.io/"

resolvers += Resolver.file("Local Ivy Repository", file("/home/julianpeeters/.ivy2/local/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq( 
  // "com.novus" %% "salat" % "1.9.1", 
  "io.spray" %%  "spray-json" % "1.2.5",
  //"com.julianpeeters" %% "asm-salat-example" % "0.1-SNAPSHOT",
  "com.gensler" %% "scalavro" % "0.4.0",
  "org.ow2.asm" % "asm-all" % "4.1",
  "com.novus" %% "salat" % "1.9.3"
)
