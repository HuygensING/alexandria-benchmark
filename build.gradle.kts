plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.72"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    maven {
        url = uri("http://maven.huygens.knaw.nl/repository")
    }
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use parsec.
    implementation("lambdada:parsec:1.0")

    // Use Arrow for fp
    val arrowVersion = "0.10.3"
    implementation("io.arrow-kt:arrow-core:${arrowVersion}")
    implementation("io.arrow-kt:arrow-core-data:${arrowVersion}")

    implementation("nl.knaw.huygens.alexandria:alexandria-markup-core:2.3")

    // Use tablesaw for visualisation
    val tablesawVersion = "0.38.1"
    implementation("tech.tablesaw:tablesaw-core:${tablesawVersion}")
    implementation("tech.tablesaw:tablesaw-jsplot:${tablesawVersion}")  //  for creating charts
    implementation("tech.tablesaw:tablesaw-html:${tablesawVersion}")    // for using HTML
//    implementation("tech.tablesaw:tablesaw-beakerx:${tablesawVersion}") // for using Tablesaw inside BeakerX
//    implementation("tech.tablesaw:tablesaw-excel:${tablesawVersion}")   //  for using Excel workbooks
//    implementation("tech.tablesaw:tablesaw-json:${tablesawVersion}")    // for using JSON

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("org.assertj:assertj-core:3.12.2")
    testImplementation("commons-io:commons-io:2.6")
}
