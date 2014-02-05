package com.dgwave.car.repo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.IOUtil;

/**
 * Static utilities to be used for Ceylon repos.
 * 
 * @author Akber Choudhry
 */
public final class CeylonUtil {
    
    /**
     * Path separator.
     */
    public static final char PATH_SEPARATOR = '/';

    /**
     * Group separator.
     */
    public static final char GROUP_SEPARATOR = '.';
    
    /**
     * Artifact separator.
     */
    public static final char ARTIFACT_SEPARATOR = '-';

    /**
     * Initial StringBuilder size.
     */
    public static final int STRING_BUILDER_SIZE = 128;
    
    /**
     * Initial size of the buffer used to calculate checksums.
     */
    private static final int CHECKSUM_BUFFER_SIZE = 65536 * 2;

    /**
     * Number of ways in which a Ceylon Java dependencies can be represented.
     */
    public static final int NUM_CEYLON_JAVA_DEP_TYPES = 3;
    
    /**
     * Hidden constructor.
     */
    private CeylonUtil() {
        
    }
    
    /**
     * Replaces '.' with '/' to derive module path.
     * @param dotSep The dot-separated name to process
     * @return String The path with '.' replaced by '/'
     */
    public static String formatAsDirectory(final String dotSep) {
        if (dotSep == null || "".equals(dotSep)) {
            return "";
        }
        return dotSep.replace(GROUP_SEPARATOR, PATH_SEPARATOR);
    }
    
    /**
     * Determines the relative path of a module atifact within a Ceylon repo.
     * 
     * @param groupId Maven group id
     * @param artifactId Maven artifact id
     * @param version Version
     * @param classifier Sources, javadoc etc.
     * @param extension The extension or packaging of the artifact
     * @return String The computed path
     */
    public static String ceylonRepoPath(final String groupId, final String artifactId, final String version,
            final String classifier, final String extension) {
        
        StringBuilder path = new StringBuilder(STRING_BUILDER_SIZE);
    
        path.append(formatAsDirectory(groupId)).append(PATH_SEPARATOR);
        path.append(artifactId).append(PATH_SEPARATOR);
        path.append(version).append(PATH_SEPARATOR);
        path.append(ceylonModuleName(groupId, artifactId, version, classifier, extension));
    
        return path.toString();
    }

    /**
     * Comes up with a Ceylon module name based on Maven coordinates.
     * 
     * @param groupId Maven group id
     * @param artifactId Maven artifact id
     * @param version Version
     * @param classifier Sources, javadoc etc.
     * @param extension The extension or packaging of the artifact
     * @return String The module name
     */
    public static String ceylonModuleName(final String groupId, final String artifactId, final String version, 
            final String classifier, final String extension) {
        
        if (version == null || "".equals(version) || version.contains("-")) {
         // TODO fix the '-' based on the new Herd rules
            throw new IllegalArgumentException(" Null, empty, or '-' is not allowed in version");
        }
        
        StringBuilder name = new StringBuilder(STRING_BUILDER_SIZE);
        
        name.append(ceylonModuleBaseName(groupId, artifactId))
            .append(ARTIFACT_SEPARATOR).append(version);
    
        if (extension != null) {
            name.append(GROUP_SEPARATOR).append(extension);
        } else {
            name.append(GROUP_SEPARATOR).append("car");
        }
        
        return name.toString();
    }

    /**
     * Determines the Ceylon module name (without extension).
     * 
     * @param groupId Maven group id
     * @param artifactId Maven artifact id
     * @return String The module base name
     */
    public static String ceylonModuleBaseName(final String groupId, final String artifactId) {
        if (groupId == null || artifactId == null || "".equals(artifactId) || artifactId.contains(".")) {
            throw new IllegalArgumentException(" Null or empty, or '.' is not allowed in artifactId");
        }
        return groupId + GROUP_SEPARATOR +  artifactId;
    }
    
    /**
     * Calculates the SHA1 checksum for a file using a crude method.
     * 
     * @param installedFile  The installed artifact for which the checksum is being computed
     * @return String The SHA1 checksum
     * @throws MojoExecutionException In case of IO error or checksum calculation error
     */
    public static String calculateChecksum(final File installedFile) throws MojoExecutionException {
        int bufsize = CHECKSUM_BUFFER_SIZE;
        byte[] buffer = new byte[bufsize];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        String checksum = null;

        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            
            fis = new FileInputStream(installedFile);
            sha1.reset();
            int size = fis.read(buffer, 0, bufsize);
            while (size >= 0) {
                sha1.update(buffer, 0, size);
                size = fis.read(buffer, 0, bufsize);
            }
            checksum = String.format("%040x", new BigInteger(1, sha1.digest()));
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to calculate digest checksum for " + installedFile, e);
        } finally {
            IOUtil.close(bis);
            IOUtil.close(fis);
        }
        
        if (checksum != null) {
            return checksum;
        } else {
            throw new MojoExecutionException("Failed to calculate digest checksum for " + installedFile); 
        }
    }    
}
