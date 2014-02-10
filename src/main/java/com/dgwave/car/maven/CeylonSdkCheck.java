package com.dgwave.car.maven;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static com.dgwave.car.repo.CeylonUtil.CEYLON_REPO;;

/**
 * Looks up <code>ceylon.repo</code> or <code>CEYLON_HOME</code> system properties and tries to locate
 * the Ceylon system repository. Finally, checks a default location, <code>~/.ceylon-1.x.x/repo</code>.
 * 
 * @author Akber Choudhry
 */
@Mojo(name = "sdk-check", requiresProject = false, aggregator = true, threadSafe = false,
    defaultPhase = LifecyclePhase.INITIALIZE)
public class CeylonSdkCheck extends AbstractMojo {

    /**
     * Set this to <code>true</code> to bypass Ceylon SDK validation or installation.
     */
    @Parameter(property = "ceylon.sdk.skip", defaultValue = "false")
    private boolean skip;  
    
    /**
     * The Ceylon system repo.
     */
    private File sysRepo;
    
     /**
     *  Execute Mojo.
     *  @see org.apache.maven.plugin.Mojo#execute()
     *  @throws MojoExecutionException In case SDK is not found, or other error
     */
    public void execute() throws MojoExecutionException {
        
        if (skip) {
            getLog().info("Skipping ceylon:sdk-check");
            return;
        }
        
        if (System.getProperty(CEYLON_REPO) != null) {
            getLog().debug("System property ceylon.repo found: " + System.getProperty(CEYLON_REPO));  
            sysRepo = new File(System.getProperty(CEYLON_REPO));
        } else {
            getLog().debug("System property ceylon.repo not found");
        }
        
        if (sysRepo == null && System.getenv("CEYLON_HOME") != null) {
            getLog().debug("Environment variable CEYLON_HOME found : " + System.getenv("CEYLON_HOME"));            
            sysRepo = new File(System.getenv("CEYLON_HOME"), "repo");
        } else {
            getLog().debug("Environment variable CEYLON_HOME not found");
        }
        
        if (sysRepo == null) {
            String dotCeylon = System.getProperty("user.home") 
                   + File.separator + ".ceylon";
            
            File hDir = findSdkHome(dotCeylon);
            
            if (hDir != null) {
                File r = new File(hDir, "repo");
                if (r != null && r.isDirectory()) {
                    sysRepo = r;
                }
            }
        }
        
        if (sysRepo == null) {
            throw new MojoExecutionException("Ceylon system repo (ceylon.repo or CEYLON_HOME) not set,"
                + " or repo not found in them, or in {user.home}/.ceylon/ceylon-1.x.x/repo. "
                + "Please use goal ceylon:sdk-download");
        } else if (!sysRepo.isDirectory()) {
            throw new MojoExecutionException("Ceylon system repo found but does not exist or is not a directory");
        } else {
            System.setProperty(CEYLON_REPO, sysRepo.getAbsolutePath());
            getLog().info("Ceylon system repo set to " + System.getProperty(CEYLON_REPO));
        }  
    }

    /**
     * Find Ceylon SDK home in a folder, in the form of 'ceylon-1.x.x'.
     * @param path Folder path to search in
     * @return File folder whose name is alphabetically highest, or null if not found.
     */
    File findSdkHome(final String path) {
        File dotCeylon = new File(path); // or some other directory
        
        File[] homes = null;
        
        if (!dotCeylon.exists()) {
            return null;
        } else {
            homes = dotCeylon.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(final File dir, final String name) {
                    if (name.startsWith("ceylon-1.")) {
                        return true;
                    }
                    return false;
                }
            });
        }
        
        if (homes.length == 0) {
            return null;
        } else if (homes.length == 1 & homes[0].isDirectory()) {
            return homes[0];
        } else {
            Arrays.sort(homes);
            if (homes[homes.length - 1].isDirectory()) {
                return homes[homes.length - 1];
            } else {
                return null;
            }
        }
    }
}