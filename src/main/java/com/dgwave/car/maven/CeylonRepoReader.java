package com.dgwave.car.maven;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;

import com.dgwave.car.common.CeylonUtil;

/**
 * For future IDE support.
 * @author Akber Choudhry
 */
@Component (role = WorkspaceReader.class, hint = "ide")
public class CeylonRepoReader implements WorkspaceReader {
    
    @Requirement
    private Logger logger;
    
    /* (non-Javadoc)
     * @see org.eclipse.aether.repository.WorkspaceReader#getRepository()
     */
    @Override
    public WorkspaceRepository getRepository() {
        return new WorkspaceRepository("ceylon", "dotCeylonConfig");
    }

    /* (non-Javadoc)
     * @see org.eclipse.aether.repository.WorkspaceReader#findArtifact(org.eclipse.aether.artifact.Artifact)
     */
    @Override
    public File findArtifact(final Artifact artifact) {
        String type = artifact.getProperty("type", "jar");
        if ("ceylon-jar".equals(type) || "car".equals(type)) {
            if ("ceylon-jar".equals(type)) {
                type = "jar";
            }

            File art = new File(CeylonUtil.ceylonSystemFullPath(artifact, type));
            
            if (art.isFile()) {
            	 if (logger != null) {
            		 logger.info("Resolved from Ceylon repo: " + artifact);
            	 }
                artifact.setFile(art);
                return art;
            }
        } 
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.aether.repository.WorkspaceReader#findVersions(org.eclipse.aether.artifact.Artifact)
     */
    @Override
    public List<String> findVersions(final Artifact artifact) {
        return Arrays.asList(new String[]{artifact.getVersion()});
    }

}
