name := "Crawlang"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
	"org.apache.poi"			% "poi"					% "3.10-FINAL",
	"com.restfb"				% "restfb"				% "1.6.14",
	"org.slf4j"					% "slf4j-api"			% "1.7.7",
	"com.google.gdata"			% "core"				% "1.47.1",
	"org.twitter4j"				% "twitter4j-core"		% "4.0.2",
	"org.quartz-scheduler"		% "quartz"				% "2.2.1",
	"org.quartz-scheduler"		% "quartz-jobs"			% "2.2.1",
	"org.mongodb.morphia"		% "morphia"				% "0.108",
	"org.jongo"					% "jongo"				% "1.1",
	"uk.co.panaxiom"			% "play-jongo_2.11"		% "0.7.1-jongo1.0",
	"com.optimaize.languagedetector" % "language-detector" % "0.4",
	jdbc,
	javaJpa,
	cache,
	javaWs
)
