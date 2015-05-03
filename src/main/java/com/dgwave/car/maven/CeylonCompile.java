package com.dgwave.car.maven;

import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Looks up 'ceylon.repo' or CEYLON_HOME/repo system property for the Ceylon system repository. 
 * If not found, aborts the build unless the 'download' property is set to true.
 * @author Akber Choudhry
 */
@Execute(goal = "sdk-check")
@Mojo(name = "compile", requiresProject = true, aggregator = false, threadSafe = false,
    defaultPhase = LifecyclePhase.COMPILE)
public class CeylonCompile extends AbstractMojo {
    
  /**
     *  Execute Mojo.
     *  @see org.apache.maven.plugin.Mojo#execute()
     *  @throws MojoExecutionException In case SDK is not found and download is not true, or other error
     */
    public void execute() throws MojoExecutionException {
        for (Entry<Object, Object> prop : System.getProperties().entrySet()) {

        }
    }
}