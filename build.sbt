name := "toolbox-salat-example"

version := "0.0.1-SNAPSHOT"

organization := "com.julianpeeters"

scalaVersion := "2.10.1"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq( 
  // "com.novus" %% "salat" % "1.9.1", 
   "com.novus" %% "salat" % "1.9.2-SNAPSHOT",
   "org.slf4j" % "slf4j-simple" % "1.6.4"
)
