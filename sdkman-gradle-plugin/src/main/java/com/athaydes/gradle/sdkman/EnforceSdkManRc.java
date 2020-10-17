package com.athaydes.gradle.sdkman;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class EnforceSdkManRc extends DefaultTask {

    @InputFile
    private final File sdkmanrc;

    @InputFile
    private final File gradleBuildFile;

    public EnforceSdkManRc() {
        sdkmanrc = new File( getProject().getProjectDir(), ".sdkmanrc" );
        gradleBuildFile = getProject().getBuildFile();
    }

    @TaskAction
    public void enforceSdkManRc() {
        String javaHome = System.getenv( "JAVA_HOME" );
        String currentJavaVersion = findJavaFullVersionFromJavaHome( javaHome );

        String sdkmanJavaVersion;
        try {
            sdkmanJavaVersion = readSdkManRcJavaVersion();
        } catch ( IOException e ) {
            throw new GradleException( "SDKMAN! Plugin unable to read .sdkmanrc file" );
        }

        getLogger().debug( ".sdkmanrc java version={}, current java version={}",
                sdkmanJavaVersion, currentJavaVersion );

        if ( !sdkmanJavaVersion.equals( currentJavaVersion ) ) {
            throw new GradleException( String.format(
                    "SDKMAN! .sdkmanrc java version (%s) does not match this process' Java version (%s).\n" +
                            "Run 'sdkman env' to set the proper Java version.",
                    sdkmanJavaVersion, currentJavaVersion ) );
        }
    }

    private String readSdkManRcJavaVersion() throws IOException {
        if ( !sdkmanrc.isFile() ) {
            throw new GradleException( "SDKMAN! Plugin requires file .sdkmanrc to exist. " +
                    "Please run 'sdkman env init' to create it.\n" +
                    "For more information visit https://sdkman.io/usage#env" );
        }
        Properties props = new Properties();
        try ( InputStream is = Files.newInputStream( sdkmanrc.toPath(), StandardOpenOption.READ ) ) {
            props.load( is );
        }
        Object javaValue = props.get( "java" );

        if ( javaValue == null ) {
            throw new GradleException( "SDKAMN! .sdkmanrc file is missing the java property.\n" +
                    "Please set it, or delete the file and re-run 'sdkman env init'." );
        }

        return javaValue.toString();
    }

    static String findJavaFullVersionFromJavaHome( String javaHome ) {
        String candidatesJavaPath = Paths.get( "candidates", "java" ).toString() + File.separator;
        int candidatesJavaPathIndex = javaHome.indexOf( candidatesJavaPath );

        if ( candidatesJavaPathIndex < 0 ) {
            throw new GradleException( "JAVA_HOME env var does not seem to be set to SDKMAN " +
                    "(should contain 'candidates/java/<version>')! JAVA_HOME=" + javaHome );
        }

        int javaVersionIndex = candidatesJavaPathIndex + candidatesJavaPath.length();
        int nextPathSeparatorIndex = javaHome.indexOf( File.separator, javaVersionIndex + 1 );

        if ( nextPathSeparatorIndex < 0 ) {
            nextPathSeparatorIndex = javaHome.length();
        }

        return javaHome.substring( javaVersionIndex, nextPathSeparatorIndex );
    }

}
