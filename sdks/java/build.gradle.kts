plugins {
    id("java-library")
    id("scala")
    id("maven-publish")
}

allprojects {
    group = "cloud.golem"
    version = "0.0.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "scala")
    apply(plugin = "maven-publish")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<ScalaCompile> {
        scalaCompileOptions.additionalParameters = listOf("-feature", "-deprecation")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("Golem SDK for Java and Scala")
                    url.set("https://golem.cloud")

                    licenses {
                        license {
                            name.set("Apache License 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                        }
                    }

                    developers {
                        developer {
                            id.set("golemcloud")
                            name.set("Golem Cloud")
                            email.set("contact@golem.cloud")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/golemcloud/golem.git")
                        developerConnection.set("scm:git:ssh://github.com/golemcloud/golem.git")
                        url.set("https://github.com/golemcloud/golem")
                    }
                }
            }
        }
    }
}
