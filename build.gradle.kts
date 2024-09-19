/*
 * Copyright (c) 2024 Leon Linhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import groovy.util.Node
import groovy.util.NodeList
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.js.yarn.*
import org.jetbrains.kotlin.gradle.targets.jvm.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(buildDeps.plugins.binary.compatibility.validator)
    alias(buildDeps.plugins.dokkatoo.html)
//    alias(buildDeps.plugins.dokkatoo.javadoc)
    alias(buildDeps.plugins.kotlin.multiplatform)
    alias(buildDeps.plugins.kotlin.plugin.serialization)
    id("com.gw2tb.maven-publish-conventions")
}

yarn.lockFileName = "kotlin-yarn.lock"
yarn.lockFileDirectory = rootDir

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                apiVersion = KotlinVersion.KOTLIN_1_9
                languageVersion = KotlinVersion.KOTLIN_1_9
            }
        }
    }

    js {
        browser()
        nodejs()
    }

    jvm {
        withJava()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget = JvmTarget.JVM_11
                freeCompilerArgs.add("-Xjdk-release=11")
            }
        }
    }

//    androidNativeArm32()
//    androidNativeArm64()
//    androidNativeX64()
//    androidNativeX86()

    linuxArm64()
    linuxX64()

    iosArm64()
    iosX64()

    iosSimulatorArm64()

    macosArm64()
    macosX64()

    mingwX64()

    tvosArm64()
    tvosX64()

    tvosSimulatorArm64()

//    wasmJs()
//    wasmWasi()

    watchosArm32()
    watchosArm64()
    watchosX64()

//    watchosDeviceArm64()
    watchosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.serialization.core)
                api(libs.ktor.client.websockets)

                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
                implementation(buildDeps.turbine)
            }
        }

        val websocketServerTest by creating {
            dependsOn(commonTest.get())

            dependencies {
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.server.websockets)
            }
        }

        named("appleTest") {
            dependsOn(websocketServerTest)
        }

        named("linuxTest") {
            dependsOn(websocketServerTest)
        }

        named("jvmTest") {
            dependsOn(websocketServerTest)
        }
    }

    targets.filter { it is KotlinJvmTarget || it is KotlinWithJavaTarget<*, *> }.forEach { target ->
        tasks.named<Jar>(target.artifactsTaskName) {
            manifest {
                attributes(mapOf(
                    "Name" to project.name,
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>"
                ))
            }
        }
    }
}

dokkatoo {
    dokkaGeneratorIsolation = ProcessIsolation {
        maxHeapSize = "4G"
    }

    dokkatooSourceSets.configureEach {
        reportUndocumented = true
        skipEmptyPackages = true
        jdkVersion = 11

        val localKotlinSourceDir = layout.projectDirectory.dir("src/$name/kotlin")
        val version = project.version

        sourceLink {
            localDirectory = localKotlinSourceDir

            remoteUrl("https://github.com/GW2ToolBelt/drf-api-client/tree/v${version}/src/main/kotlin")
            remoteLineSuffix = "#L"
        }
    }

    dokkatooPublications.configureEach {
        moduleName = "drf-api-client"

        // TODO Remaining warnings are silly atm. Reevaluate this flag in the future.
//        failOnWarning = true
    }

    versions {
        jetbrainsDokka = buildDeps.versions.dokka
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.javaModuleVersion = "$version"
        options.release = 11
    }

    named<JavaCompile>("compileJava") {
        options.compilerArgumentProviders += object : CommandLineArgumentProvider {

            @InputFiles
            @PathSensitive(PathSensitivity.RELATIVE)
            val kotlinClasses = this@tasks.named<KotlinCompile>("compileKotlinJvm").flatMap(KotlinCompile::destinationDirectory)

            override fun asArguments() = listOf(
                "--patch-module",
                "com.gw2tb.drf.api=${kotlinClasses.get().asFile.absolutePath}"
            )

        }
    }

    withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
    }

    dokkatooGeneratePublicationHtml {
        outputDirectory = layout.projectDirectory.dir("docs/site/api")
    }

    // See https://github.com/GW2ToolBelt/GW2ChatLinks/issues/18
    generatePomFileForKotlinMultiplatformPublication {
        dependsOn(project.tasks.named("generatePomFileForJvmPublication"))
    }
}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            val emptyJavadocJar = tasks.register<Jar>("${name}JavadocJar") {
                archiveBaseName = "${archiveBaseName.get()}-${name}"
                archiveClassifier = "javadoc"
            }

            artifact(emptyJavadocJar)
        }

        // See https://github.com/GW2ToolBelt/GW2ChatLinks/issues/18
        named<MavenPublication>("kotlinMultiplatform") {
            val jvmPublication = publications.getByName<MavenPublication>("jvm")

            lateinit var jvmXml: XmlProvider
            jvmPublication.pom?.withXml { jvmXml = this }

            pom.withXml {
                val xmlProvider = this
                val root = xmlProvider.asNode()
                // Remove the original content and add the content from the platform POM:
                root.children().toList().forEach { root.remove(it as Node) }
                jvmXml.asNode().children().forEach { root.append(it as Node) }

                // Adjust the self artifact ID, as it should match the root module's coordinates:
                ((root.get("artifactId") as NodeList).first() as Node).setValue(artifactId)

                // Set packaging to POM to indicate that there's no artifact:
                root.appendNode("packaging", "pom")

                // Remove the original platform dependencies and add a single dependency on the platform
                // module:
                val dependencies = (root.get("dependencies") as NodeList).first() as Node
                dependencies.children().toList().forEach { dependencies.remove(it as Node) }
                val singleDependency = dependencies.appendNode("dependency")
                singleDependency.appendNode("groupId", jvmPublication.groupId)
                singleDependency.appendNode("artifactId", jvmPublication.artifactId)
                singleDependency.appendNode("version", jvmPublication.version)
                singleDependency.appendNode("scope", "compile")
            }
        }
    }
}