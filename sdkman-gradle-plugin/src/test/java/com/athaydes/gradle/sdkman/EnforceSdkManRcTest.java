package com.athaydes.gradle.sdkman;

import org.gradle.api.GradleException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class EnforceSdkManRcTest {

    public static final String CANDIDATES_JAVA_9_0_4_OPEN_CONTENTS_HOME =
            "/candidates/java/9.0.4-open/Contents/Home";

    public static final String HOME_CANDIDATES_JAVA_11_0_8_FX_ZULU =
            "/home/joe/.sdkman/candidates/java/11.0.8.fx-zulu";
    public static final String VERSION_11_0_8_FX_ZULU = "11.0.8.fx-zulu";
    public static final String VERSION_9_0_4_OPEN = "9.0.4-open";
    public static final String VERSION_8_0_265_ZULU = "8.0.265-zulu";

    @Test
    public void canFindJavaFullVersionFromJavaHomeInLocalMachine() {
        assertEquals(
                VERSION_11_0_8_FX_ZULU,
                EnforceSdkManRc.findJavaVersionInLocalSdkManInstallation(HOME_CANDIDATES_JAVA_11_0_8_FX_ZULU).orElseThrow());

        assertEquals(
                VERSION_9_0_4_OPEN,
                EnforceSdkManRc.findJavaVersionInLocalSdkManInstallation(CANDIDATES_JAVA_9_0_4_OPEN_CONTENTS_HOME).orElseThrow());
    }

    @Test
    public void cannotFindJavaFullVersionIfJavaHomeIsNotSdkManJavaLocally() {
        assertFalse(EnforceSdkManRc.findJavaVersionInLocalSdkManInstallation("").isPresent());
        assertFalse(EnforceSdkManRc.findJavaVersionInLocalSdkManInstallation("/usr/java").isPresent());
        assertFalse(EnforceSdkManRc.findJavaVersionInLocalSdkManInstallation("/home/me/.sdkman/foo/java").isPresent());
    }

    @Test
    public void canFindJavaVersionByLookingAtJavaHomePath() throws IOException {
        File dir = java.nio.file.Files.createTempDirectory("TempDirectory").toFile();

        File java8Home = new File(dir, "hostedtoolcache/java-8.0.265-zulu/1.0.0/x64");
        File java9Home = new File(dir, "hostedtoolcache/java-9.0.4-open/1.0.0/x86");

        assertTrue(java8Home.mkdirs());
        assertTrue(java9Home.mkdirs());

        assertTrue(EnforceSdkManRc.isJavaVersionInJavaHomePath(java8Home.getAbsolutePath(), VERSION_8_0_265_ZULU));
        assertTrue(EnforceSdkManRc.isJavaVersionInJavaHomePath(java9Home.getAbsolutePath(), VERSION_9_0_4_OPEN));

        assertFalse(EnforceSdkManRc.isJavaVersionInJavaHomePath(java8Home.getAbsolutePath(), VERSION_9_0_4_OPEN));
        assertFalse(EnforceSdkManRc.isJavaVersionInJavaHomePath(java9Home.getAbsolutePath(), VERSION_8_0_265_ZULU));
        dir.deleteOnExit();
    }

    @Test
    public void canEnforceJavaHomeMatchesSdkManRcJavaVersionInLocalInstallation() {

        // should be ok
        EnforceSdkManRc.enforce(HOME_CANDIDATES_JAVA_11_0_8_FX_ZULU, VERSION_11_0_8_FX_ZULU);
        EnforceSdkManRc.enforce(CANDIDATES_JAVA_9_0_4_OPEN_CONTENTS_HOME, VERSION_9_0_4_OPEN);

        assertGradleException("JAVA_HOME (" + CANDIDATES_JAVA_9_0_4_OPEN_CONTENTS_HOME +
                        ") seems to indicate Java version " + VERSION_9_0_4_OPEN +
                        ", but .sdkmanrc Java version is " + VERSION_11_0_8_FX_ZULU,
                () -> EnforceSdkManRc.enforce(
                        CANDIDATES_JAVA_9_0_4_OPEN_CONTENTS_HOME, VERSION_11_0_8_FX_ZULU));

        assertGradleException("JAVA_HOME (" + HOME_CANDIDATES_JAVA_11_0_8_FX_ZULU +
                        ") seems to indicate Java version " + VERSION_11_0_8_FX_ZULU +
                        ", but .sdkmanrc Java version is " + VERSION_9_0_4_OPEN,
                () -> EnforceSdkManRc.enforce(
                        HOME_CANDIDATES_JAVA_11_0_8_FX_ZULU, VERSION_9_0_4_OPEN));
    }

    @Test
    public void canEnforceJavaHomeMatchesSdkManRcJavaVersionInCI() throws IOException {
        File dir = java.nio.file.Files.createTempDirectory("TempDirectory").toFile();

        File java8Home = new File(dir, "hostedtoolcache/java-8.0.265-zulu/1.0.0/x64");
        File java9Home = new File(dir, "hostedtoolcache/java-9.0.4-open/1.0.0/x86");

        assertTrue(java8Home.mkdirs());
        assertTrue(java9Home.mkdirs());

        // should be ok
        EnforceSdkManRc.enforce(java8Home.getAbsolutePath(), VERSION_8_0_265_ZULU);
        EnforceSdkManRc.enforce(java9Home.getAbsolutePath(), VERSION_9_0_4_OPEN);

        assertGradleException("JAVA_HOME (" + java9Home.getAbsolutePath() +
                        ") does not seem to contain a Java version matching the .sdkmanrc version (" +
                        VERSION_8_0_265_ZULU + ")",
                () -> EnforceSdkManRc.enforce(
                        java9Home.getAbsolutePath(), VERSION_8_0_265_ZULU));

        assertGradleException("JAVA_HOME (" + java8Home.getAbsolutePath() +
                        ") does not seem to contain a Java version matching the .sdkmanrc version (" +
                        VERSION_9_0_4_OPEN + ")",
                () -> EnforceSdkManRc.enforce(
                        java8Home.getAbsolutePath(), VERSION_9_0_4_OPEN));
        dir.deleteOnExit();
    }

    private static void assertGradleException(String message, Runnable runnable) {
        try {
            runnable.run();
            fail("Expected GradleException but nothing was thrown, expected error: '" + message + "'");
        } catch (GradleException e) {
            assertEquals(message, e.getMessage());
        }
    }
}
