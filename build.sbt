name := "PoE"
version := "0.2"

scalaVersion := "3.1.1"
// format: off
javacOptions ++= Seq("-Xlint", "-encoding", "UTF-8")
scalacOptions ++= Seq(
  "-explain",                          // Explain errors in more detail.
  "-explain-types",                    // Explain type errors in more detail.
  "-indent",                           // Allow significant indentation.
  "-new-syntax",                       // Require `then` and `do` in control expressions.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-source:future",                    // better-monadic-for
  "-language:implicitConversions",     // Allow implicit conversions
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:postfixOps",              // Explicitly enables the postfix ops feature
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  //"-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  //"-Xmigration:3.1",                   // Warn about constructs whose behavior may have changed since version.
  //"-Xsemanticdb",                      // Store information in SemanticDB.
  //"-Xcheck-macros",
  //"-Ycook-comments",                   // Cook the comments (type check `@usecase`, etc.)
  //"-Yretain-trees",                    // Retain trees for top-level classes, accessible from ClassSymbol#tree
  //"-Yexplicit-nulls",                  // Make reference types non-nullable. Nullable types can be expressed with unions: e.g. String|Null.
  //"-Yshow-suppressed-errors",          // Also show follow-on errors and warnings that are normally suppressed.
  //"-rewrite",
  //"-source", "future-migration",
  //"-migration", "future-migration",
)
// format: on

val circeVersion = "0.14.1"
val fs2Version = "3.2.4"
libraryDependencies ++= Seq(
  "org.typelevel"         %% "cats-effect"         % "3.3.5",
  "com.github.kwhat"       % "jnativehook"         % "2.2.1",
  "com.github.pureconfig" %% "pureconfig-core"     % "0.17.1",
  "io.circe"             %% "circe-core"           % circeVersion,
  "io.circe"             %% "circe-parser"         % circeVersion,
  "co.fs2"               %% "fs2-core"             % fs2Version,
  "co.fs2"               %% "fs2-io"               % fs2Version,
)