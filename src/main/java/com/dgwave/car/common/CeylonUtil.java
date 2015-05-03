package com.dgwave.car.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
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
     * Regular buffer size.
     */
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * Number of ways in which a Ceylon Java dependencies can be represented.
     */
    public static final int NUM_CEYLON_JAVA_DEP_TYPES = 4;

    /**
     * ceylon.repo.
     */
    public static final String CEYLON_REPO = "ceylon.repo";
    
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

    public static String ceylonSystemFullPath(org.eclipse.aether.artifact.Artifact artifact, String type) {
        // TODO map to .ceylon/config mapping
        return systemRepo() + File.separator
            + ceylonRepoPath(artifact.getGroupId(), artifact.getArtifactId(), 
                artifact.getVersion(), artifact.getClassifier(), type);
    }

    public static String systemRepo() {
        return System.getProperty("ceylon.repo", System.getProperty("user.home")
            + File.separator + ".ceylon" + File.separator + "repo");
    }
    
    public static String ceylonSystemFullPath(Artifact artifact, String type) {

        // TODO map to .ceylon/config mapping
        return systemRepo()
        
            + File.separator
            
            + ceylonRepoPath(artifact.getGroupId(), artifact.getArtifactId(), 
            artifact.getVersion(), artifact.getClassifier(), type);
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

    /**
     * Extracts a single file from a zip archive.
     * @param in Input zip stream
     * @param outdir Output directory
     * @param name File name
     * @throws IOException In case of IO error
     */
    public static void extractFile(final ZipInputStream in, final File outdir, final String name) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)));
        int count = -1;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
        out.close();
    }

    /**
     * Creates directories from an existing file to an additional path.
     * @param outdir Existing directory
     * @param path Additional path
     */
    public static void mkdirs(final File outdir, final String path) {
        File d = new File(outdir, path);
        if (!d.exists()) {
            d.mkdirs();
        }
    }

    /**
     * Returns the directory part of path string.
     * @param path Input path
     * @return String The directory part of the path
     */
    public static String dirpart(final String path) {
        int s = path.lastIndexOf(File.separatorChar);
        if (s != -1) {
            return path.substring(0, s);
        } else {
            return null;
        } 
    }

    /***
     * Extract zipfile to outdir with complete directory structure.
     * 
     * @param zipfile Input .zip file
     * @param outdir Output directory
     */
    public static void extract(final File zipfile, final File outdir) {
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
            ZipEntry entry;
            String name, dir;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                if (entry.isDirectory()) {
                    mkdirs(outdir, name);
                    continue;
                }

                dir = dirpart(name);
                if (dir != null) {
                    mkdirs(outdir, dir);
                }

                extractFile(zin, outdir, name);
            }
            zin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Artifact toArtifact(String name, String version) {
        int d = name.lastIndexOf('.');
        String g = null, a = null;
        if (d == -1) {
            g = name; a = name;
        } else {
            g = name.substring(0, d); a = name.substring(d + 1);
        }
        return new DefaultArtifact(g, a, version, null, null, null, null);
    } 
}
