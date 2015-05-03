package com.dgwave.car.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CeylonWorkspaceReaderTest {

	CeylonWorkspaceReader reader;
    @Before
    public void setUp() throws Exception {
        reader = new CeylonWorkspaceReader(null,
        		new ConsoleLogger());
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
        Artifact artifact = testArtifact();

        File file = reader.findArtifact(artifact);

        assertNotNull(file);
    }

	private Artifact testArtifact() {
		Map<String, String> props = new HashMap<String, String>();
        props.put("type", "ceylon-jar");
        Artifact artifact = new DefaultArtifact("com.redhat.ceylon.compiler", "java",
            null, "jar", "1.1.0", props, (ArtifactType)null);
		return artifact;
	}

    @Test
    public void testFindVersions() {
        List<String> list = reader.findVersions(testArtifact());
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("1.1.0", list.get(0));
    }
}
