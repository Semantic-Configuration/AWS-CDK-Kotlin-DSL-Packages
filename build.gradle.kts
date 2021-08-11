plugins {
  scala
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
}

tasks.wrapper {
  gradleVersion = "7.1.1"
}
tasks.create("stage", Delete::class) {
  dependsOn(tasks["shadowJar"])

  delete(
    ".gitignore",
    fileTree(".gradle"),
    fileTree(".").exclude("/.*", "/build/libs/${project.name}-all.jar")
  )
  doLast {
    file("Procfile").writeText("web: java -jar build/libs/${project.name}-all.jar")
  }
}
application.mainClass.set("io.lemm.chamelania.Main")


repositories {
  mavenCentral()
  maven("https://jitpack.io")
}
dependencies {
  val v = "2.13"
  implementation("org.scala-lang", "scala-library", "$v.6")
  implementation("com.chuusai", "shapeless_$v", "2.3.7")

  implementation("io.github.portfoligno.porterie", "porterie_$v", "0.4.0")
  implementation("org.http4s", "http4s-blaze-client_$v", "1.0.0-M24")
  implementation("is.cir", "ciris_$v", "2.1.0")
  runtimeOnly("org.slf4j", "slf4j-simple", "1.7.32")
}
