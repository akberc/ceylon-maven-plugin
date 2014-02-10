package com.dgwave.car.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.util.FileUtils;

import com.dgwave.car.repo.CeylonUtil;

/**
 * Looks up 'ceylon.repo' or CEYLON_HOME/repo system property for the Ceylon system repository. 
 * If not found, aborts the build unless the 'download' property is set to true.
 * @author Akber Choudhry
 */
@Mojo(name = "sdk-download", requiresProject = false, aggregator = true, threadSafe = false,
    defaultPhase = LifecyclePhase.NONE)
public class CeylonSdkDownload extends AbstractMojo {
    
    /**
     * Set this to <code>true</code> to bypass Ceylon SDK validation or installation.
     */
    @Parameter(property = "ceylon.sdk.skip", defaultValue = "false")
    private boolean skip;
  
    /**
     * If download is true, specify directory to download to. Defaults to {user.home}/.ceylon.
     */
    @Parameter(property = "ceylon.sdk.downloadTo", defaultValue = "{user.home}/.ceylon")
    private String downloadTo;
  
    /**
     * If download is true, the URL from which to get the SDK. Defaults to {user.home}/.ceylon/sdk.
     */
    @Parameter(property = "ceylon.sdk.fromURL", 
        defaultValue = "http://downloads.ceylon-lang.org/cli/ceylon-1.0.0.zip")
    private String fromURL;

    /**
     * HTTP wagon.
     * 
     */
    @Component(role = Wagon.class, hint = "http")
    private Wagon wagon;
    
    /**
     * The Ceylon system repo.
     */
    private File sysRepo;
    
     /**
     *  Execute Mojo.
     *  @see org.apache.maven.plugin.Mojo#execute()
     *  @throws MojoExecutionException In case SDK is not found and download is not true, or other error
     */
    public void execute() throws MojoExecutionException {
        
        if (skip) {
            getLog().info("Skipping ceylon:sdk-download");
            return;
        }
        
        if (System.getProperty("ceylon.repo") != null) {
            throw new MojoExecutionException("Ceylon system repo (ceylon.repo system property) is already set to: "
                + System.getProperty("ceylon.repo"));
        }
        

            
        if ("{user.home}/.ceylon".equals(downloadTo)) { // replace placeholder
            downloadTo = System.getProperty("user.home") 
               + File.separator + ".ceylon";
            getLog().debug("downloadTo property not set, using default");
        }
        
        sysRepo = downloadSdk(downloadTo);
        
        if (sysRepo == null || !sysRepo.exists()) {
            String message = "Download specified but target directory exists or could not be created";
            getLog().info(message);
        } else {
            System.setProperty("ceylon.repo", sysRepo.getAbsolutePath());
            getLog().debug("Downloaded Ceylon SDK and set Ceylon system repo to: " + System.getProperty("ceylon.repo"));
        }  
    }

    /**
     * Download the Ceylon SDK.
     * @param path Where to download
     * @return File The location of the SDK
     * @throws MojoExecutionException In case of download error
     */
    private File downloadSdk(final String path) throws MojoExecutionException {
        File dotCeylon = new File(path); // or some other directory
        
        if (!dotCeylon.exists()) {
            dotCeylon.mkdirs();
        }
        
        String[] segments = this.fromURL.split("/");
        String file = segments[segments.length - 1];
        String repoUrl = this.fromURL.substring(0, this.fromURL.length() - file.length());
        String version = FileUtils.basename(file);
        
        File home = new File(dotCeylon, version);
        
        if (home.exists()) {
            getLog().info("Skipping download: Folder " + version + " corresponding to SDK already exists "
                + " in " + dotCeylon.getAbsolutePath());
            return new File(home, "repo");
        }
       
        File outputFile = new File(dotCeylon, file);
        
        if (outputFile.exists()) {
            throw new MojoExecutionException("Downloaded file " + outputFile.getAbsolutePath() 
                + " exists. Please remove it and try again");
        }
       
        try {
            doGet(outputFile, repoUrl);
        } catch (Exception e) {
            throw new MojoExecutionException("Error downloading SDK" + e.getLocalizedMessage(), e);
        }
        
        CeylonUtil.extract(outputFile, dotCeylon);
        
        outputFile.delete();
        
        return new File(home, "repo");
    }
    
    /**
     * Does the actual retrieval.
     * @param outputFile The file to write to
     * @param repoUrl 
     * @throws Exception When SDK cannot be retrieved
     * TODO add proxy and listener and externalize timeout
     */
    private void doGet(final File outputFile, final String repoUrl) throws Exception {
        int readTimeOut = 5 * 60 * 1000;

        Repository repository = new Repository(repoUrl, repoUrl);
        
        wagon.setReadTimeout(readTimeOut);
        getLog().info("Read Timeout is set to " + readTimeOut + " milliseconds");
        wagon.connect(repository);
        wagon.get(outputFile.getName(), outputFile);
        wagon.disconnect();
      }
}