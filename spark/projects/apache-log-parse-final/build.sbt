name := "Apache Log Parsing"

version := "0.0.1"

scalaVersion := "2.11.8"

// additional libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.1" % "provided",
  "org.apache.spark" % "spark-sql_2.11" % "2.1.1" % "provided",
  "org.scala-lang" % "scala-reflect" % "2.11.8" % "provided",
  "com.holdenkarau" %% "spark-testing-base" % "1.5.2_0.6.0" % "test"
  // "org.scalatest" % "scalatest_2.11" % "3.0.1" % "test",
)
