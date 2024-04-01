# DRF API Client

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
| Kotlin/JS                                | `jvm`                                                                 |
| iOS                                      | `iosArm64`, `iosX64`, `iosSimulatorArm64`                             |
| watchOS                                  | `watchosArm32`, `watchosArm64`, `watchosX64`, `watchosSimulatorArm64` |
| tvOS                                     | `tvosArm64`, `tvosX64`, `tvosSimulatorArm64`                          |
| macOS                                    | `macosArm64`, `macosX64`                                              |
| Linux                                    | `macosArm64`, `macosX64`                                              |
| Windows                                  | `linuxArm64`, `linuxX64`                                              |
| Windows                                  | `mingwX64`                                                            |
