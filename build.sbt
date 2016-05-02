name := """hermes-server"""

version := "0.0.2-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  jdbc,
  javaEbean,
  "org.webjars" % "jquery" % "2.1.1",
  "org.webjars" % "bootstrap" % "3.3.1",
  "mysql" % "mysql-connector-java" % "5.1.36"
)

lazy val root = (project in file(".")).enablePlugins(PlayJava)


fork in run := true