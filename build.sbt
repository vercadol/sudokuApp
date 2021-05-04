name := "sudokuApp"
 
version := "1.0" 
      
lazy val `sudokuapp` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice,
  "com.adrianhurt" %% "play-bootstrap" % "1.6.1-P28-B4",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "test")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

routesImport += "binders.GameModeBinder._"
routesImport += "controllers.GameMode._"
