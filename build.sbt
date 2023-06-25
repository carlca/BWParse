name := "BWParse"

version := "1.0"

scalaVersion := "3.3.0"

//javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
//resolvers += "Maven Central" at "https://repo1.maven.org/maven2/"

scalacOptions ++= Seq("-indent", "-rewrite")

libraryDependencies ++= Seq(
)

// Define project structure
lazy val root = (project in file("."))
  .settings(
)