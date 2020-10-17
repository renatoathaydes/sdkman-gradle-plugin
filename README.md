## SDKMAN! Gradle Plugin

[https://renatoathaydes.github.io/sdkman-gradle-plugin](https://renatoathaydes.github.io/sdkman-gradle-plugin/index.html)

This plugin makes sure that [Gradle](https://gradle.org) is using a specific JDK, provided by [SDKMAN!](https://sdkman.io),
to build your project.

It does not run SDKMAN! itself, it only checks if the Java version you expect is being used (as explained below).

Because SDKMAN!'s JDK identifiers includes both a Java version and a vendor, with this plugin you can guarantee that
your project will be compiled and tested against a specific JDK, like `11.0.2-open` or `11.0.8.fx-zulu`.

### What does this Plugin check

This plugin checks the following:

* the project contains a [`sdk env`](https://sdkman.io/usage#env) file `.sdkmanrc`.
* the `.sdkmanrc` specifies a `java` property with a SDKMAN! JDK identifier.
* the Gradle process is running the JDK specified by `.sdkmanrc`.
* The `JAVA_HOME` environment variable is pointing to the same JDK.
* the project's `sourceCompatibility` is compatible with the JDK being used.

This ensures that you never try to build your project with the wrong JDK, which can cause build errors, or worse,
releasing a project that was tested on a different JVM.

### Applying this Plugin

Add this plugin to your `plugins` block:

```groovy
plugins {
    id 'com.athaydes.sdkman'
}
```

Or using the older syntax:

```groovy
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "com.athaydes.gradle:sdkman-gradle-plugin:<version>"
    }
}

apply plugin: 'com.athaydes.sdkman'
apply plugin: 'java'
```

### Requirements

* Gradle 5.0+
* Java 8+

### Usage

You only need to apply this plugin to your project in order to use it.

There's currently no configuration.

Because this plugin won't run `sdk` for you, you're expected to run it manually, or automate it in your CI.

Example workflow:

```
cd my-project
sdk env           # load the JVM specified in .sdkmanrc
./gradlew build
```

> Notice that `sdk env` is only required if you did not enable SDKMAN!'s `sdkman_auto_env` property to `true`.
> See [SDKMAN! Config](https://sdkman.io/usage#config) for instructions on setting properties.

The value this plugin provides is that if you forget to run `sdk env`, your build will fail, preventing you from making
mistakes.

### Examples

Please find a few examples inside the [examples](examples) directory.

### Tasks

* `checkJavaVersion`  - checks that the Java versions (env, sys props, `sourceCompatibility`) all match.
* `enforceSdkManRc`   - enforces that the Java home matches the `.sdkmanrc` JDK identifier.

Dependency graph:

```
AbstractCompile (all tasks of type)
    dependsOn enforceSdkManRc
        dependsOn checkJavaVersionTask
```

Because all compilation tasks depend on this plugin's tasks, you should never need to run this plugin's tasks
explicitly (but you can, if you want).

If anything goes wrong, the build fails with an error explaining what's wrong in detail.
