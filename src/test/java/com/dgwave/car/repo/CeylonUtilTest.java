package com.dgwave.car.repo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.dgwave.car.common.CeylonUtil;

public class CeylonUtilTest {

    @Test
    public void testFormatAsDirectory() {
        try {
            CeylonUtil.formatAsDirectory(null);
            CeylonUtil.formatAsDirectory("");
        } catch (Exception e) {
            fail("Exception in conversion from . to /");
        }
        assertEquals("abc", CeylonUtil.formatAsDirectory("abc"));
        assertEquals("/a/bc/", CeylonUtil.formatAsDirectory(".a.bc."));
    }

    @Test
    public void testCeylonModuleBaseName() {
        assertEquals("junit.junit", CeylonUtil.ceylonModuleBaseName("junit", "junit"));
        assertEquals("org.junit.junit", CeylonUtil.ceylonModuleBaseName("org.junit", "junit"));
        assertEquals("org.junit.junit-base", CeylonUtil.ceylonModuleBaseName("org.junit", "junit-base"));
        try {
            assertEquals("null.null", CeylonUtil.ceylonModuleBaseName(null, null));
            fail ("Illegal argument exception should have been thrown for null artifactId");
        } catch (Exception e) {}   
        try {
            assertEquals("org.junit.junit-base", CeylonUtil.ceylonModuleBaseName("org.junit", "junit.base"));
            fail ("Illegal argument exception should have been thrown for . in artifactId");
        } catch (Exception e) {}
    }

    @Test
    public void testCeylonModuleName() {
        try {
            assertEquals("null.null", 
                CeylonUtil.ceylonModuleName(null, null, null, null, null));
            fail ("Illegal argument exception should have been thrown for nulls");            
        } catch (Exception e) {}   
        try {            
            assertEquals("org.junit.junit-base", 
                CeylonUtil.ceylonModuleName("org.junit", "junit-base", null, null, null));
            fail ("Illegal argument exception should have been thrown for . no version");
        } catch (Exception e) {
            // excepted
        }
        assertEquals("org.junit.junit-base-4.10.car", 
            CeylonUtil.ceylonModuleName("org.junit", "junit-base", "4.10", null, null));        
    }
    
    @Test
    public void testCeylonRepoPath() {

        assertEquals("org/junit/junit-base/4.10/org.junit.junit-base-4.10.car", 
            CeylonUtil.ceylonRepoPath("org.junit", "junit-base", "4.10", null, null)); 
    }

}
