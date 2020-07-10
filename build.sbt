inThisBuild(
  Seq(
    scalaVersion := "2.12.11",
    scalacOptions ++= Seq(
      "-Ypartial-unification",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
      "-Ywarn-inaccessible",           // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any",              // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit",           // Warn when nullary methods return Unit.
      "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
      "-Ywarn-unused:locals",          // Warn if a local definition is unused.
      "-Ywarn-unused:params",          // Warn if a value parameter is unused.
      "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",        // Warn if a private member is unused.
      "-Ypartial-unification"          // Only from 2.12.2 on - upgrade if you haven't yet.
    )
  )
)

val circeVersion = "0.13.0"
val http4sVersion = "0.21.6"
val pac4jVersion = "3.8.3"
val http4sPac4jVersion = "1.0.0"
val specs2Version = "4.10.0"
val logbackVersion = "1.2.3"

val Deps = Seq(
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-scalatags" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.pac4j" %% "http4s-pac4j" % http4sPac4jVersion,
  "org.pac4j" % "pac4j-core" % pac4jVersion,
  "org.pac4j" % "pac4j-http" % pac4jVersion,
  "org.pac4j" % "pac4j-oauth" % pac4jVersion,
  "org.pac4j" % "pac4j-oidc" % pac4jVersion,
  "org.pac4j" % "pac4j-openid" % pac4jVersion,
  "com.lihaoyi" %% "scalatags" % "0.8.2",
  "org.slf4j" % "slf4j-api" % "1.7.26",
  "org.http4s" %% "http4s-server" % http4sVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

lazy val simpleSsoExample = project
 .in(file("."))
 .settings(
   libraryDependencies ++= Deps,
    //It's require for getting of net.shibboleth.tool:xmlsectool
    resolvers += "opensaml Repository" at
      "https://build.shibboleth.net/nexus/content/repositories/releases",
    Compile / compile / wartremoverWarnings ++= Warts.allBut(
      Wart.Any, // wartremover/wartremover#263
      Wart.Nothing, // wartremover/wartremover#263
      Wart.ImplicitParameter, // we know what we're doing, right?
      Wart.DefaultArguments, // we know what we're doing, right?
    )
  )
