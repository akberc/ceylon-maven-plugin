package com.dgwave.car.repo;

import static org.junit.Assert.assertEquals;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dgwave.car.repo.CeylonRepoLayout;

public class CeylonRepoLayoutTest {

    CeylonRepoLayout layout;
    
    @Before
    public final void setUp() throws Exception {
        layout = new CeylonRepoLayout();
    }

    @After
    public final void tearDown() throws Exception {
        layout = null;
    }

    @Test
    public final void testGetId() {
       assertEquals("ceylon", layout.getId());
    }

    @Test
    public final void testPathOfArtifact() {
        Artifact artifact = new DefaultArtifact(
            "junit", "junit-base", "4.10", "test", "jar", null,
            new DefaultArtifactHandler("jar")
            );
        assertEquals("junit/junit-base/4.10/junit.junit-base-4.10.jar", layout.pathOf(artifact));
    }
}
