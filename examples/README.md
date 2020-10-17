## Gradle SDKMAN! Plugin Examples

This directory contains examples of how to use the Gradle SDKMAN! Plugin.

### Simple

Minimal example: does not configure anything in `build.gradle`.

Contains a [`.sdkmanrc`](simple/.sdkmanrc) file as it's mandatory when applying the SDKMAN! Plugin.

Relies on Gradle using the correct Java version based on that.

### Full example

Besides the [`.sdkmanrc`](full-example/.sdkmanrc) file, this example also configures the Java `sourceCompatibility`,
which the SDKMAN! Plugin will check to make sure it matches the compiler version being used.

It also has a [`gradle.properties`](full-example/gradle.properties) file to instruct Gradle to use a specific Java home.
This value is commented-out because it does not match the SDKMAN! Java Home! If you want, you can try un-commenting it
and you'll see that the SDKMAN! Plugin will see there's a mismatch and fail the build.

The Java class it compiles uses a class from the `javafx` package, which will only compile if the JDK itself includes
JavaFX (which it should if the JDK from `.sdkmanrc` is used to compile).
