/*
 * Copyright (c) 2024-2026 Leon Linhart
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
gradle.allprojects {
    pluginManager.apply(MavenArtifactBundlePlugin::class)
}

public class MavenArtifactBundlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val localRepo = project.rootProject.layout.buildDirectory.dir("localRepo")

        if (project == project.rootProject) {
            project.tasks.register<Tar>("artifactBundle") {
                dependsOn(project.provider {
                    buildList {
                        val projects = project.subprojects + project

                        for (project in projects) {
                            if (project.pluginManager.hasPlugin("maven-publish")
                            ) {
                                add(project.tasks.named("publishAllPublicationsToIntermediateBundlingRepository"))
                            }
                        }
                    }
                })

                compression = Compression.GZIP

                // TODO Investigate why using conventions is insufficient for KMP projects
                destinationDirectory.set(project.layout.buildDirectory.dir("libs"))
                archiveFileName.set("maven-artifact-bundle.tgz")

                from(localRepo) {
                    exclude("**/maven-metadata.xml")
                    exclude("**/maven-metadata.xml.*")
                }
            }
        }

        project.pluginManager.withPlugin("maven-publish") {
            val publishing = project.extensions.getByType<PublishingExtension>()

            publishing.repositories.maven {
                name = "IntermediateBundling"
                url = project.uri(localRepo)

                metadataSources {
                    mavenPom()
                }
            }
        }
    }

}
