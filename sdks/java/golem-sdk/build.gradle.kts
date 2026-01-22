plugins {
    id("java-library")
    id("scala")
}

dependencies {
    // Scala standard library
    implementation("org.scala-lang:scala-library:2.13.12")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.scalatest:scalatest_2.13:3.2.17")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("co.helmethair:scalatest-junit-runner:0.2.0")
}

tasks.test {
    useJUnitPlatform()
}
