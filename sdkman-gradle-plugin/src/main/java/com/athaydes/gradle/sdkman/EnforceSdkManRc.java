package com.athaydes.gradle.sdkman;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
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
        String sdkmanJavaVersion;
        try {
            sdkmanJavaVersion = readSdkManRcJavaVersion();
        } catch ( IOException e ) {
            throw new GradleException( "SDKMAN! Plugin unable to read .sdkmanrc file" );
        }

        String javaHome = System.getenv( "JAVA_HOME" );

        enforce( javaHome, sdkmanJavaVersion );
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

    static void enforce( String javaHome,
                         String sdkManJavaVersion ) {
        Optional<String> actualVersion = findJavaVersionInLocalSdkManInstallation( javaHome );
        String error = "";
        if ( actualVersion.isPresent() ) {
            if ( !actualVersion.get().equals( sdkManJavaVersion ) ) {
                error = "JAVA_HOME (" + javaHome + ") seems to indicate Java version " + actualVersion.get() +
                        ", but .sdkmanrc Java version is " + sdkManJavaVersion;
            }
        } else {
            if ( !isJavaVersionInJavaHomePath( javaHome, sdkManJavaVersion ) ) {
                error = "JAVA_HOME (" + javaHome + ") does not seem to contain a Java version matching the" +
                        " .sdkmanrc version (" + sdkManJavaVersion + ")";
            }
        }
        if ( !error.isEmpty() ) {
            throw new GradleException( error );
        }
    }

    static Optional<String> findJavaVersionInLocalSdkManInstallation( String javaHome ) {
        String candidatesJavaPath = Paths.get( "candidates", "java" ).toString() + File.separator;
        int candidatesJavaPathIndex = javaHome.indexOf( candidatesJavaPath );

        if ( candidatesJavaPathIndex < 0 ) {
            return Optional.empty();
        }

        int javaVersionIndex = candidatesJavaPathIndex + candidatesJavaPath.length();
        int nextPathSeparatorIndex = javaHome.indexOf( File.separator, javaVersionIndex + 1 );

        if ( nextPathSeparatorIndex < 0 ) {
            nextPathSeparatorIndex = javaHome.length();
        }

        return Optional.of( javaHome.substring( javaVersionIndex, nextPathSeparatorIndex ) );
    }

    static boolean isJavaVersionInJavaHomePath( String javaHome, String expectedVersion ) {
        File file = new File( javaHome );
        if ( !file.isDirectory() ) {
            return false;
        }
        int levels = 0;
        final int maxLevels = 4;
        while ( file != null && levels < maxLevels ) {
            if ( isVersionMatch( file.toPath(), expectedVersion ) ) {
                return true;
            }
            file = file.getParentFile();
            levels++;
        }
        return false;
    }

    private static boolean isVersionMatch( Path path, String expectedVersion ) {
        String name = Optional.ofNullable( path.getFileName() )
                .map( Object::toString )
                .orElse( "" );

        if ( name.startsWith( "java-" ) ) {
            name = name.substring( "java-".length() );
        }

        return name.equals( expectedVersion );
    }

}
