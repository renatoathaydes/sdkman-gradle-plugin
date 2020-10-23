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
import java.util.HashSet;
import java.util.Set;

public class CheckJavaVersion extends DefaultTask {

    @TaskAction
    public void checkJavaVersion() {
        String javaHomeProp = System.getProperty( "java.home" );
        String javaHomeEnv = System.getenv( "JAVA_HOME" );

        if ( javaHomeEnv == null ) {
            throw new GradleException( "JAVA_HOME environment variable is not set. " +
                    "Please run 'sdk env' or set it manually." );
        }

        checkJavaHomeConsistency( javaHomeProp, javaHomeEnv );
        checkSourceCompatibility();
    }

    private void checkJavaHomeConsistency( String javaHomeProp, String javaHomeEnv ) {
        File prop = new File( javaHomeProp );
        File env = new File( javaHomeEnv );

        if ( !env.isDirectory() ) {
            throw new GradleException( "JAVA_HOME is not a directory: " + javaHomeEnv );
        }

        if ( javaHomeProp.equals( javaHomeEnv ) ) {
            getLogger().debug( "JAVA_HOME matches java.home system property exactly: {}", javaHomeEnv );
            return; // ok
        }

        // if the java tool resolves to the same exact path, the differences may be due to symlinks being used
        Path javaFromProp;
        try {
            javaFromProp = prop.toPath().resolve( Paths.get( "bin", "java" ) ).toRealPath();
        } catch ( IOException e ) {
            throw new GradleException( "Unable to find bin/java file under system property java.home=" + prop );
        }

        Set<Path> candidateJavaLocations = getCandidateJavaLocations( env.toPath() );

        if ( getLogger().isInfoEnabled() ) {
            getLogger().info( "java from 'java.home' system property: {}", javaFromProp );
            getLogger().info( "java candidates from 'JAVA_HOME' env var: {}", candidateJavaLocations );
        }

        if ( candidateJavaLocations.contains( javaFromProp ) ) {
            return; // ok
        }

        throw new GradleException( "JAVA_HOME env var does not match the current process.\n" +
                "Current java process:    " + javaFromProp + "\n" +
                "Possible java locations: " + javaHomeEnv );
    }

    private Set<Path> getCandidateJavaLocations( Path javaHome ) {
        Path binJava = null;
        try {
            binJava = javaHome.resolve( Paths.get( "bin", "java" ) ).toRealPath();
        } catch ( IOException e ) {
            getLogger().info( "bin/java does not exist under JAVA_HOME={}", javaHome );
        }
        Path jreBinJava = null;
        try {
            jreBinJava = javaHome.resolve( Paths.get( "jre", "bin", "java" ) ).toRealPath();
        } catch ( IOException e ) {
            getLogger().info( "jre/bin/java does not exist under JAVA_HOME={}", javaHome );
        }

        Set<Path> result = new HashSet<>( 2 );
        if ( binJava != null ) {
            result.add( binJava );
        }
        if ( jreBinJava != null ) {
            result.add( jreBinJava );
        }
        if ( result.isEmpty() ) {
            throw new GradleException( "Cannot find bin/java or jre/bin/java under JAVA_HOME=" + javaHome );
        }
        return result;
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
