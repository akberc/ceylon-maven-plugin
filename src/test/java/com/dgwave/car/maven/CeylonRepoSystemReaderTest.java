package com.dgwave.car.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CeylonRepoSystemReaderTest {

    CeylonRepoReader reader;
    @Before
    public void setUp() throws Exception {
        reader = new CeylonRepoReader();
    }

    @After
    public void tearDown() throws Exception {
        reader = null;
    }

    @Test
    public void testGetRepository() {
        WorkspaceRepository repo = reader.getRepository();
        assertNotNull(repo);
        assertEquals("ceylon", repo.getContentType());
    }

    @Test
    public void testFindArtifact() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("type", "ceylon-jar");
        Artifact artifact = new DefaultArtifact("com.redhat.ceylon.compiler", "java",
            null, "jar", "1.1.0", props, (ArtifactType)null);

        File file = reader.findArtifact(artifact);

        assertNotNull(file);
    }

    @Test
    public void testFindVersions() {
        List<String> list = reader.findVersions(
            new DefaultArtifact("com.redhat.ceylon.compiler:java:1.0.0"));
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("1.0.0", list.get(0));
    }
}
