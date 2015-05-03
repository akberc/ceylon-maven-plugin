package com.dgwave.car.repo;

import java.io.File;
import java.util.Collection;

import org.eclipse.aether.spi.connector.ArtifactDownload;
import org.eclipse.aether.spi.connector.ArtifactUpload;
import org.eclipse.aether.spi.connector.MetadataDownload;
import org.eclipse.aether.spi.connector.MetadataUpload;
import org.eclipse.aether.spi.connector.RepositoryConnector;
import org.eclipse.aether.spi.connector.Transfer.State;
import org.eclipse.aether.spi.log.Logger;

import com.dgwave.car.common.CeylonUtil;

public class CeylonRepositoryConnector implements RepositoryConnector {
	

	private Logger logger;

	public CeylonRepositoryConnector(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void get(Collection<? extends ArtifactDownload> artifactDownloads,
			Collection<? extends MetadataDownload> metadataDownloads) {

		if (metadataDownloads != null) {
			for (MetadataDownload download : metadataDownloads) {
				System.out.println("Requesting: " + download.toString());
			}
		}
		
		for (ArtifactDownload download : artifactDownloads) {
			if (download.getArtifact() != null 
					&& "car".equals(download.getArtifact().getExtension())) {
				try {
					download.setState(State.DONE);
					
					download.setFile(
							new File(
						CeylonUtil.ceylonSystemFullPath(download.getArtifact(), 
								download.getArtifact().getExtension())));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void put(Collection<? extends ArtifactUpload> artifactUploads,
			Collection<? extends MetadataUpload> metadataUploads) {
		// not implemented
	}

	@Override
	public void close() {
		// nothing
	}
}
