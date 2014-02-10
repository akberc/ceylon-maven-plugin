package com.dgwave.car.maven;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dgwave.car.repo.CeylonUtil;

public class CeylonSdkCheckTest {

    private CeylonSdkCheck mojo;
    private Log log = new SystemStreamLog() ;
    
    @Before
    public void setUp() throws Exception {
        mojo = new CeylonSdkCheck();
        mojo.setLog(log);
    }

    @After
    public void tearDown() throws Exception {
        if (mojo != null) {
            mojo = null;
        }
    }

    @Test
    public void testExecute() {
        System.setProperty(CeylonUtil.CEYLON_REPO, "some.thing");
        try {
            mojo.execute();
            fail("SDK Check should not throw exception");
        } catch (MojoExecutionException e) {
            assertEquals("some.thing", System.getProperty(CeylonUtil.CEYLON_REPO));
        }
    }

    @Test
    public void testFindSdkHome() {
        try {
            String path = System.getProperty("user.home") + File.separator + ".ceylon";
            mojo.findSdkHome(path);
        } catch (Exception e) {
            fail("Failed finding SDK home in given path");
        }
    }
}
