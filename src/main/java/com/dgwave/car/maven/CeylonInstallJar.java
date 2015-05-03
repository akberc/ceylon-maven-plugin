package com.dgwave.car.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.dgwave.car.repo.CeylonRepoLayout;
import com.dgwave.car.common.CeylonUtil;

/**
 * Project context not required. use the `-Dfile` parameter to point to a jar file.  A `pom.xml`
 * file in the same directory or within the jar file will be parsed for dependencies. Installs into the 
 * Ceylon 'user' repository. Target can be changed to `cache` or `local`.
 * @author Akber Choudhry
 */
@Mojo(name = "install-jar", requiresProject = false, aggregator = true, threadSafe = false)
public class CeylonInstallJar extends CeylonInstall {
    /**
     * GroupId of the artifact to be installed. Retrieved from POM file if one is specified or extracted from
     * {@code pom.xml} in jar if available.
     */
    @Parameter(property = "groupId")
    private String groupId;

    /**
     * ArtifactId of the artifact to be installed. Retrieved from POM file if one is specified or extracted from
     * {@code pom.xml} in jar if available.
     */
    @Parameter(property = "artifactId")
    private String artifactId;

    /**
     * Version of the artifact to be installed. Retrieved from POM file if one is specified or extracted from
     * {@code pom.xml} in jar if available.
     */
    @Parameter(property = "version")
    private String version;
    
    /**
     * The file to be installed in the local repository.
     */
    @Parameter(property = "jar")
    private File jar;
  
    /**
     * Location of an existing POM file to be installed alongside the main artifact, given by the {@link #jar}
     * parameter.
     * 
     * @since 2.1
     */
    @Parameter(property = "pomFile")
    private File pomFile;
    
    /**
     * The ceylon repository to install to. 'user', 'cache' and 'local' are supported.
     */
    @Parameter(property = "repo", defaultValue = "user")
    private String ceylonRepository;

    /**
     * The repository base directory.
     */
    private File repositoryPath;
    
    /**
     * The local Ceylon repository.
     */
    private ArtifactRepository localRepository;

    /**
     * The type or packaging of the artifact being installed.
     */
    private String packaging;
 
    /**
     * A Maven project model of the artifact project.
     */
    private Model model;
    /**
     *  Execute Mojo.
     *  @see org.apache.maven.plugin.Mojo#execute()
     *  @throws MojoExecutionException In case of an error
     */
    public void execute() throws MojoExecutionException {
        
        if (jar == null || !jar.exists()) {
            if (gavExists()) { // attempt to resolve jar from GAV
                DefaultArtifactResolver resolver = new DefaultArtifactResolver();
                ArtifactResolutionResult result = resolver.resolve(new ArtifactResolutionRequest()
                    .setArtifact(new DefaultArtifact(groupId, artifactId, version, null, "jar", null, 
                        new DefaultArtifactHandler("jar")))
                );
                
                if (result.isSuccess()) {
                    jar = result.getArtifacts().iterator().next().getFile();
                } else {
                    String message = "Jar file '" + jar.getPath() + "' not specified and "
                        + "GAV coordinates could not be resolved";
                    getLog().error(message);
                    throw new MojoExecutionException(message);                    
                }
            } else {
                String message = "Jar file '" + jar.getPath() + "' does not exixt and "
                    + "GAV coordinates not specified";
                getLog().error(message);
                throw new MojoExecutionException(message);
            }
        }
        
        try {
            repositoryPath = findRepoPath(this.ceylonRepository);
            ArtifactRepositoryLayout layout = new CeylonRepoLayout();
            getLog().debug("Layout: " + layout.getClass());

            localRepository =
                new DefaultArtifactRepository(ceylonRepository, repositoryPath.toURI().toURL().toString(), layout);
            getLog().debug("Repository: " +  localRepository);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("MalformedURLException: " + e.getMessage(), e);
        }
        
        if (pomFile != null) { // explicitly specified
           processModel(readModel(pomFile));
        } else if (new File(jar.getParentFile(), "pom.xml").exists()) { // pom.xml in the same directory
            processModel(readModel(new File(jar.getParentFile(), "pom.xml"))); 
        } else {
            boolean foundPom = false;

            try {
                Pattern pomEntry = Pattern.compile("META-INF/maven/.*/pom\\.xml");

                JarFile jarFile = new JarFile(jar);

                Enumeration<JarEntry> jarEntries = jarFile.entries();

                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = jarEntries.nextElement();

                    if (pomEntry.matcher(entry.getName()).matches()) {
                        getLog().debug("Using " + entry.getName() + " for groupId, artifactId, packaging and version");
                        foundPom = true;
                        InputStream pomInputStream = null;

                        try {
                            pomInputStream = jarFile.getInputStream(entry);
                            processModel(readModel(pomInputStream));
                            break;
                        } finally {
                            if (pomInputStream != null) {
                                pomInputStream.close();
                            } 
                            if (jarFile != null) {
                                jarFile.close();
                            }                            
                        }
                    }
                }

                if (!foundPom) {
                    getLog().info("pom.xml not found in " + jar.getName());
                }
            } catch (IOException e) {
                getLog().warn("This jar file was not packaged by Maven");
            }
        }
        // if packaging is set, check it
        if (packaging != null & !"jar".equals(packaging)) {
            throw new MojoExecutionException("POM packaging is not 'jar' ");
        }
        
        if (!jar.getName().endsWith(".jar")) {
            throw new MojoExecutionException("Jar File extension must be 'jar'  ");
        }
    
        if (groupId == null || "".equals(groupId)) {
            throw new MojoExecutionException("Group Id is empty  ");
        }
  
        if (artifactId == null || "".equals(artifactId)) {
            throw new MojoExecutionException("Artifact Id is empty  ");
        }
 
        if (version == null || "".equals(version)) {
            throw new MojoExecutionException("Version is empty  ");
        }
        
        installJar(jar);
    }

    /**
     * Determines if the GAV coordinates are specified.
     * @return True if all three exist, false otherwise
     */
    private boolean gavExists() {
        return groupId != null && !"".equals(groupId)
            && artifactId != null && !"".equals(artifactId)
            && version != null && !"".equals(version);
    }

    /**
     * Does the actual installation of the jar file.
     * @param installableFile The file to install
     * @throws MojoExecutionException In case of an error
     */
    private void installJar(final File installableFile) throws MojoExecutionException {
        try {
            Artifact artifact = new DefaultArtifact(groupId, 
                artifactId, version,
                null, "jar",
                null, new DefaultArtifactHandler("jar"));
            
            install(installableFile, artifact, localRepository);
            
            File artifactFile = new File(localRepository.getBasedir(), 
                localRepository.pathOf(artifact));

            installAdditional(artifactFile, ".sha1", 
                CeylonUtil.calculateChecksum(artifactFile), false);
            
            if (model != null) {
                String deps = calculateDependencies(new MavenProject(model));

                if (!"".equals(deps)) {
                    installAdditional(artifactFile, "module.properties", deps, false);
                    installAdditional(artifactFile, ".module", deps, true);
                }
            }            
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Populates missing mojo parameters from the specified POM.
     * 
     * @param readModel The POM to extract missing artifact coordinates from, 
     * must not be <code>null</code>.
     */
    private void processModel(final Model readModel) {
        this.model = readModel;
        
        Parent parent = readModel.getParent();

        if (this.groupId == null) {
            this.groupId = readModel.getGroupId();
            if (this.groupId == null && parent != null) {
                this.groupId = parent.getGroupId();
            }
        }
        if (this.artifactId == null) {
            this.artifactId = readModel.getArtifactId();
        }
        if (this.version == null) {
            this.version = readModel.getVersion();
            if (this.version == null && parent != null) {
                this.version = parent.getVersion();
            }
        }
        if (this.packaging == null) {
            this.packaging = readModel.getPackaging();
        }
    }
    
    /**
     * Parses a POM.
     * 
     * @param pomStream The path of the POM file to parse, must not be <code>null</code>.
     * @return The model from the POM file, never <code>null</code>.
     * @throws MojoExecutionException If the POM could not be parsed.
     */
    private Model readModel(final InputStream pomStream) throws MojoExecutionException {
        Reader reader = null;
        try {
            reader = ReaderFactory.newXmlReader(pomStream);
            return new MavenXpp3Reader().read(reader);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException("File not found " + pomFile, e);
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading POM " + pomFile, e);
        } catch (XmlPullParserException e) {
            throw new MojoExecutionException("Error parsing POM " + pomFile, e);
        } finally {
            IOUtil.close(reader);
        }
    }    
}