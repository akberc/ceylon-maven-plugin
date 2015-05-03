package com.dgwave.car.repo;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.util.repository.layout.RepositoryLayout;

import com.dgwave.car.common.CeylonUtil;

/**
 * This class serves as the Ceylon repo layout for both the Apache and Aether layout interfaces.
 * @author Akber Choudhry
 */
@Component(role = ArtifactRepositoryLayout.class, hint = "ceylon")
public class CeylonRepoLayout
    extends DefaultRepositoryLayout implements ArtifactRepositoryLayout, RepositoryLayout
{

    /**
     * @return String The layout id
     * @see org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout#getId()
     */
    public String getId() {
        return "ceylon";
    }

    /**
     * @param artifact The artifact whose path is to be computed by the layout
     * @return String The relative path of the artifact in this layout
     * @see org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout#pathOf(Artifact)
     */
    public String pathOf(final Artifact artifact) {
        ArtifactHandler artifactHandler = artifact.getArtifactHandler();
        String classifier = null;
        String extension = null;
        if (artifact.getClassifier() != null && !"".equals(artifact.getClassifier())) {
            classifier = artifact.getClassifier();
        }
        if (artifactHandler != null 
            && artifactHandler.getExtension() != null 
            && !"".equals(artifactHandler.getExtension())) {
            extension = artifactHandler.getExtension();
        }
        return CeylonUtil.ceylonRepoPath(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), 
            classifier, extension);
    }

    /**
     * The Aether implementation.
     * @param artifact The artifact to get the path for
     * @return URI of the artifact
     * @see org.eclipse.aether.util.repository.layout.RepositoryLayout#getPath(org.eclipse.aether.artifact.Artifact)
     */
    public URI getPath(final org.eclipse.aether.artifact.Artifact artifact) {
        return toUri(pathOf(new DefaultArtifact(artifact.getGroupId(), 
            artifact.getArtifactId(), artifact.getBaseVersion(),
            "", artifact.getExtension(),
            artifact.getClassifier(), new DefaultArtifactHandler(artifact.getExtension()))));
    }

    /**
     * Not implemented.
     * @param metadata The metadata for which to find the path
     * @return null Always null as metadata is not iplemented
     * @see org.eclipse.aether.util.repository.layout.RepositoryLayout#getPath(Metadata) 
     */
    public URI getPath(final Metadata metadata) {
        return null;
    }

    /**
     * Converts from String to URI.
     * @param path The string to convert
     * @return URI if there was no exception
     */
    private URI toUri(final String path) {
        try {
            return new URI(null, null, path, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}