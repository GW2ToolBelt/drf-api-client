# DRF API Client

[![License](https://img.shields.io/badge/license-MIT-green.svg?style=for-the-badge&label=License)](https://github.com/GW2Toolbelt/drf-api-client/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.gw2tb.drf/drf-api-client.svg?style=for-the-badge&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.gw2tb.drf/drf-api-client)
[![Documentation](https://img.shields.io/maven-central/v/com.gw2tb.drf/drf-api-client.svg?style=for-the-badge&label=Documentation&color=blue)](https://gw2toolbelt.github.io/drf-api-client/)
![Kotlin](https://img.shields.io/badge/Kotlin-1%2E9-green.svg?style=for-the-badge&color=a97bff&logo=Kotlin)
![Java](https://img.shields.io/badge/Java-11-green.svg?style=for-the-badge&color=b07219&logo=Java)

A Kotlin Multiplatform library for working with the unofficial [Drop Research Facilities](https://drf.rs)
(DRF) API.

The library is fully written in common Kotlin code. Prebuilt binaries are
available for JVM (Java 11 or later), JS, Wasm, and several native targets.[^1]

[^1]: We aim to provide prebuilt libraries for all native targets supported by
Ktor. Despite that, some targets may be missing as target support may change
over time. In case something is missing, please make sure to let us know.

> [!WARNING]
> This library uses internal APIs that are not officially supported by the DRF
> developers and may break at any time.


## Usage

First, create a `DrfApiClient` instance to start using the library.

```kotlin
val drfApiClient = DrfApiClient()
```

This library uses [Ktor](https://ktor.io/) for networking. By default, a
reasonable default will be picked for the underlying [engine](https://ktor.io/docs/http-client-engines.html).
However, in some cases, it might be desirable to customize the engine.

This can be achieved by using one of the constructor overloads. For example, the
following code explicitly configures the API client to use the CIO engine.

```kotlin
val drfApiClient = DrfApiClient(CIO)
```

### Subscribing to live messages

The `subscribe` function of the API client can be used to retrieve a "cold" [flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/)
that returns live messages from the DRF API when collected.

```kotlin
val drfApiClient = ...
drfApiClient.subscribe(apiToken = "")
    .onEach { println(it) }
    .collect()
```


## Supported platforms

The following [targets](https://kotlinlang.org/docs/multiplatform-dsl-reference.html#targets) are supported by this
library:

| Target platform                          | Target preset                                                         |
|------------------------------------------|-----------------------------------------------------------------------|
| Kotlin/JVM (can also be used on Android) | `jvm`                                                                 |
| Kotlin/JS                                | `js`                                                                  |
| iOS                                      | `iosArm64`, `iosX64`, `iosSimulatorArm64`                             |
| watchOS                                  | `watchosArm32`, `watchosArm64`, `watchosX64`, `watchosSimulatorArm64` |
| tvOS                                     | `tvosArm64`, `tvosX64`, `tvosSimulatorArm64`                          |
| macOS                                    | `macosArm64`, `macosX64`                                              |
| Linux                                    | `linuxArm64`, `linuxX64`                                              |
| Windows                                  | `mingwX64`                                                            |


## Building from source

### Setup

This project uses [Gradle's toolchain support](https://docs.gradle.org/8.7/userguide/toolchains.html)
to detect and select the JDKs required to run the build. Please refer to the
build scripts to find out which toolchains are requested.

An installed JDK 1.8 (or later) is required to use Gradle.

### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the project
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

### Disclaimer

This is an **unofficial** tool. The author of this library is not associated
with ArenaNet nor with any of its partners. Modifying Guild Wars 2 through any
third party software is not supported by ArenaNet nor by any of its partners. By
using this software, you agree that it is at your own risk and that you assume
all responsibility. There is no warranty for using this software.

--------------------------------------------------------------------------------

#### drf-api-client

```
Copyright (c) 2024 Leon Linhart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

--------------------------------------------------------------------------------

#### Guild Wars 2

> © ArenaNet LLC. All rights reserved. NCSOFT, ArenaNet, Guild Wars, Guild
> Wars 2, GW2, Guild Wars 2: Heart of Thorns, Guild Wars 2: Path of Fire, Guild
> Wars 2: End of Dragons, and Guild Wars 2: Secrets of the Obscure and all
> associated logos, designs, and composite marks are trademarks or registered
> trademarks of NCSOFT Corporation.

As taken from [Guild Wars 2 Content Terms of Use](https://www.guildwars2.com/en/legal/guild-wars-2-content-terms-of-use/)
on 2024-01-23 00:57 CET.

For further information please refer to [LICENSE](LICENSE).