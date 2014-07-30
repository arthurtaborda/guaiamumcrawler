name := "Crawlang"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
	"org.hibernate"						% "hibernate-entitymanager"	% "4.3.5.Final",
	"org.apache.poi"					% "poi"											% "3.10-FINAL",
	"org.hibernate" 					% "hibernate-validator"			% "5.1.1.Final",
	"com.restfb"							% "restfb"									% "1.6.14",
	"org.slf4j"								% "slf4j-api"								% "1.7.7",
	"com.google.gdata"				% "core"										% "1.47.1",
	"com.twitter"							% "hbc-core"								% "2.0.2",
	"org.quartz-scheduler"		% "quartz"									% "2.2.1",
	"org.quartz-scheduler"		% "quartz-jobs"							% "2.2.1",
	jdbc,
	javaJpa,
	cache,
	javaWs
)
