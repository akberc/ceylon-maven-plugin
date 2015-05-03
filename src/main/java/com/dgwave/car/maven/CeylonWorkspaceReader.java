package com.dgwave.car.maven;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;

import com.dgwave.car.common.CeylonUtil;

public class CeylonWorkspaceReader implements WorkspaceReader {

	private WorkspaceReader reader;
	private Logger logger;
	private WorkspaceRepository repo;

	public CeylonWorkspaceReader(WorkspaceReader chainedReader, Logger logger) {
		this.reader = chainedReader;
		this.logger = logger;
		this.repo = new WorkspaceRepository("ceylon");
	}

	@Override
	public WorkspaceRepository getRepository() {
		return repo;
	}

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
        } else {
        	if (reader != null) {
        		return reader.findArtifact(artifact);
        	}
        }
        return null;
    }

	@Override
	public List<String> findVersions(Artifact artifact) {
        String type = artifact.getProperty("type", "jar");
        if ("ceylon-jar".equals(type) || "car".equals(type)) {
        	return Arrays.asList(new String[]{artifact.getVersion()});
        } else {
        	if (reader != null) {
        		return reader.findVersions(artifact);
        	}
        }
        return Collections.emptyList();
	}
}
