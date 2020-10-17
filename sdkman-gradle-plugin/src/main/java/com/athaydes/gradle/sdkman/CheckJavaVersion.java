package com.athaydes.gradle.sdkman;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.JavaVersion;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CheckJavaVersion extends DefaultTask {

    @TaskAction
    public void checkJavaVersion() throws IOException {
        String javaHomeProp = System.getProperty( "java.home" );
        String javaHomeEnv = System.getenv( "JAVA_HOME" );

        if ( javaHomeEnv == null ) {
            throw new GradleException( "JAVA_HOME environment variable is not set. Please run 'sdk env'." );
        }

        if ( !isSameLocation( javaHomeProp, javaHomeEnv ) ) {
            throw new GradleException( "Java home conflict.\n" +
                    "Current process  = " + javaHomeProp + "\n" +
                    "JAVA_HOME envVar = " + javaHomeEnv );
        }

        checkSourceCompatibility();
    }

    private boolean isSameLocation( String javaHomeProp, String javaHomeEnv ) throws IOException {
        File prop = new File( javaHomeProp );
        File env = new File( javaHomeEnv );

        if ( !env.isDirectory() ) {
            throw new GradleException( "JAVA_HOME is not a directory: " + javaHomeEnv );
        }

        if ( javaHomeProp.equals( javaHomeEnv ) ) {
            return true;
        }

        // if the java tool resolves to the same exact path, the differences may be due to symlinks being used
        Path javaFromProp = prop.toPath().resolve( Paths.get( "bin", "java" ) ).toRealPath();
        Path javaFromEnv = env.toPath().resolve( Paths.get( "bin", "java" ) ).toRealPath();

        if ( javaFromProp.equals( javaFromEnv ) ) {
            getLogger().info( "java canonical path: {}", javaFromProp );
            return true;
        }

        getLogger().info( "java from 'java.home' system property: {}", javaFromProp );
        getLogger().info( "java from 'JAVA_HOME' env var: {}", javaFromEnv );

        return false;
    }

    private void checkSourceCompatibility() {
        String version = System.getProperty( "java.version" );
        JavaPluginExtension javaPlugin = ( JavaPluginExtension ) getProject()
                .getConvention().findByName( "java" );

        if ( javaPlugin == null ) {
            throw new GradleException( "Java plugin has not been applied, cannot apply Sdkman! plugin" );
        }

        JavaVersion sourceCompat = javaPlugin.getSourceCompatibility();

        getLogger().debug( "sourceCompatibility is {}, java.version is {}", sourceCompat, version );

        JavaVersion javaVersion = JavaVersion.toVersion( version );

        if ( !javaVersion.equals( sourceCompat ) ) {
            getLogger().info( "sourceCompatibility has value {}, but Gradle is compiling using java version {}",
                    sourceCompat, version );
        }

        if ( javaVersion.compareTo( sourceCompat ) < 0 ) {
            throw new GradleException(
                    String.format( "sourceCompatibility (%s) cannot be higher than the java compiler version (%s)",
                            sourceCompat, javaVersion ) );
        }
    }

}
