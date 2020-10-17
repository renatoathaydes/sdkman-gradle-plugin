package com.athaydes.gradle.sdkman;

import org.gradle.api.GradleException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EnforceSdkManRcTest {

    @Test
    public void canFindJavaFullVersionFromJavaHome() {
        assertEquals(
                "11.0.8.fx-zulu",
                EnforceSdkManRc.findJavaFullVersionFromJavaHome( "/home/joe/.sdkman/candidates/java/11.0.8.fx-zulu" ) );

        assertEquals(
                "9.0.4-open",
                EnforceSdkManRc.findJavaFullVersionFromJavaHome( "/candidates/java/9.0.4-open/Contents/Home" ) );
    }

    @Test
    public void cannotFindJavaFullVersionIfJavaHomeIsNotSdkManJava() {
        assertGradleException( "JAVA_HOME env var does not seem to be set to SDKMAN " +
                "(should contain 'candidates/java/<version>')! JAVA_HOME=", () ->
                EnforceSdkManRc.findJavaFullVersionFromJavaHome( "" ) );

        assertGradleException( "JAVA_HOME env var does not seem to be set to SDKMAN " +
                "(should contain 'candidates/java/<version>')! JAVA_HOME=/usr/java", () ->
                EnforceSdkManRc.findJavaFullVersionFromJavaHome( "/usr/java" ) );

        assertGradleException( "JAVA_HOME env var does not seem to be set to SDKMAN " +
                "(should contain 'candidates/java/<version>')! JAVA_HOME=/home/me/.sdkman/foo/java", () ->
                EnforceSdkManRc.findJavaFullVersionFromJavaHome( "/home/me/.sdkman/foo/java" ) );
    }

    private static void assertGradleException( String message, Runnable runnable ) {
        try {
            runnable.run();
            fail( "Expected GradleException but nothing was thrown" );
        } catch ( GradleException e ) {
            assertEquals( message, e.getMessage() );
        }
    }

}
