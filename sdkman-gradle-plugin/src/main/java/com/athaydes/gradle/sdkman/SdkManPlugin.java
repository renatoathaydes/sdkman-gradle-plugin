package com.athaydes.gradle.sdkman;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.compile.AbstractCompile;

public class SdkManPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        CheckJavaVersion checkJavaVersionTask = project.getTasks()
                .create("checkJavaVersion", CheckJavaVersion.class);

        EnforceSdkManRc enforceSdkManRc = project.getTasks()
                .create("enforceSdkManRc", EnforceSdkManRc.class);

        enforceSdkManRc.dependsOn(checkJavaVersionTask);

        project.getTasks().withType(AbstractCompile.class, (compilation) -> {
            compilation.dependsOn(enforceSdkManRc);
        });
    }
}
