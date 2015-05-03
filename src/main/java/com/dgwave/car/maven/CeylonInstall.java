package com.dgwave.car.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.dgwave.car.common.CeylonUtil;
import com.dgwave.car.common.Module;
import com.dgwave.car.common.ModuleIdentifier;
import com.dgwave.car.repo.CeylonRepoLayout;

/**
 * From within a project, installs packaged jar artifacts into the Ceylon 'user' repository.
 * Target can be changed to 'cache' or 'local'. Reactor projects (modules) are supported.
 * Module dependencies set according to the Maven project model.
 * @author Akber Chpudhry
 */
@Mojo(name = "install", requiresProject = true, defaultPhase = LifecyclePhase.INSTALL, threadSafe = false)
public class CeylonInstall extends AbstractMojo {
    
    /**
     * Only works in a project context.
     */
    @Component
    private MavenProject project; 
   
    /**
     * If doing a multi-module project, the projects in reactor order.
     */
    @Parameter(defaultValue = "${reactorProjects}", required = true, readonly = true)
    private List<MavenProject> reactorProjects;

    /**
     * Whether every project should be deployed during its own deploy-phase or at the end of the multimodule build.
     * If set to {@code true} and the build fails, none of the reactor projects is deployed
     *
     */
    @Parameter(defaultValue = "false", property = "installAtEnd")
    private boolean installAtEnd;


    /**
     * Set this to <code>true</code> to bypass artifact installation.
     * Use this for artifacts that does not need to be installed in the local repository.
     *
     */
    @Parameter(property = "ceylon.install.skip", defaultValue = "false")
    private boolean skip;

    /**
     * The ceylon repository to install to. 'user', 'cache' and 'local' are supported.
     */
    @Parameter(property = "repo", defaultValue = "user")
    private String ceylonRepository;

    /**
     * Path to the selected repository.
     */
    private File repositoryPath;
    
    /**
     * The local Ceylon repository where the artifact will be installed.
     */
    private ArtifactRepository localRepository;
    
    /** MoJo execute.
     * @see org.apache.maven.plugin.Mojo#execute()
     * @throws MojoExecutionException In case of an error
     */
    public void execute() throws MojoExecutionException {
        try {
            repositoryPath = findRepoPath(this.ceylonRepository);
            ArtifactRepositoryLayout layout = new CeylonRepoLayout();
            getLog().debug("Layout: " + layout.getClass());

            localRepository =
                new DefaultArtifactRepository(ceylonRepository, 
                    repositoryPath.toURI().toURL().toString(), layout);
            getLog().debug("Repository: " + localRepository);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("MalformedURLException: " + e.getMessage(), e);
        }
        
        if (project.getFile() != null) {
            readModel(project.getFile());
        }
        
        if (skip) {
            getLog().info("Skipping artifact installation");
            return;
        }

        if (!installAtEnd) {
            installProject(project);
        } else {
            MavenProject lastProject = reactorProjects.get(reactorProjects.size() - 1);
            if (lastProject.equals(project)) {
                for (MavenProject reactorProject : reactorProjects) {
                    installProject(reactorProject);
                }
            } else {
                getLog().info("Installing " + project.getGroupId() + ":" + project.getArtifactId() + ":"
                                   + project.getVersion() + " at end");
            }
        }
    }

    /**
     * Does the actual installation of the project's main artifact.
     * 
     * @param mavenProject The Maven project
     * @throws MojoExecutionException In case of error while installing
     */
    private void installProject(final MavenProject mavenProject) throws MojoExecutionException {
        Artifact artifact = mavenProject.getArtifact();
        String packaging = mavenProject.getPackaging();

        boolean isPomArtifact = "pom".equals(packaging);

        try {
            // skip copying pom to Ceylon repository
            if (!isPomArtifact) {
                File file = artifact.getFile();

                if (file != null && file.isFile()) {
                    install(file, artifact, localRepository);
                    
                    File artifactFile = new File(localRepository.getBasedir(), localRepository.pathOf(artifact));
                    installAdditional(artifactFile, ".sha1", CeylonUtil.calculateChecksum(artifactFile), false);
                    String deps = calculateDependencies(mavenProject);
                    if (!"".equals(deps)) {
                        installAdditional(artifactFile, "module.properties", deps, false);
                        installAdditional(artifactFile, ".module", deps, true);
                    }
                } else {
                    throw new MojoExecutionException(
                        "The packaging for this project did not assign a file to the build artifact");
                }
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * The actual copy.
     * 
     * @param file The actual file packaged by the Maven project
     * @param artifact The artifact to be installed
     * @param repo The Ceylon repository to install to
     * @throws MojoExecutionException In case of IO error
     */
    void install(final File file, final Artifact artifact, final ArtifactRepository repo) 
            throws MojoExecutionException {
        File destFile = new File(repo.getBasedir() + File.separator
            + repo.getLayout().pathOf(artifact));
        destFile.getParentFile().mkdirs();
        try {
            FileUtils.copyFile(file, destFile);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * Determines dependencies from the Maven project model.
     * 
     * @param proj The Maven project
     * @return String of dependency lines
     * @throws MojoExecutionException In case the dependency version could not be determined
     */
    String calculateDependencies(final MavenProject proj) throws MojoExecutionException {
        
        Module module = new Module(
            new ModuleIdentifier(CeylonUtil.ceylonModuleBaseName(proj.getGroupId(), proj.getArtifactId()), 
                proj.getVersion(), false, false));
        
        for (Dependency dep : proj.getDependencies()) {
            if (dep.getVersion() != null && !"".equals(dep.getVersion())) {
                if (!"test".equals(dep.getScope()) && dep.getSystemPath() == null) {
                    module.addDependency(new ModuleIdentifier(
                        CeylonUtil.ceylonModuleBaseName(dep.getGroupId(), dep.getArtifactId()), dep.getVersion(), 
                            dep.isOptional(), false)
                    ); 
                }
            } else {
                throw new MojoExecutionException(
                    "Dependency version for " + dep + " in project " + proj 
                    + "could not be determined from the POM. Aborting.");
            }
        }
        
        StringBuilder builder = new StringBuilder(CeylonUtil.STRING_BUILDER_SIZE);
        for (ModuleIdentifier depMod : module.getDependencies()) {
            builder.append(depMod.getName());
            if (depMod.isOptional()) {
                builder.append("?");
            }
            
            builder.append("=").append(depMod.getVersion());
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * Find the Ceylon repository path from the alias.
     * 
     * @param repoAlias The Ceylon repository alias
     * @return File The file representing the path to the repo
     */
    File findRepoPath(final String repoAlias) {
        if ("user".equals(repoAlias)) {
            return new File(System.getProperty("user.home") 
                + CeylonUtil.PATH_SEPARATOR 
                + ".ceylon/repo");
        } else if ("cache".equals(repoAlias)) {
            return new File(System.getProperty("user.home") 
                + CeylonUtil.PATH_SEPARATOR 
                + ".ceylon/cache");            
        } else if ("system".equals(repoAlias)) {
            throw new IllegalArgumentException("Ceylon Repository 'system' should not be written to");
        } else if ("remote".equals(repoAlias)) {
            throw new IllegalArgumentException("Ceylon Repository 'remote' should use the ceylon:deploy Maven goal");
        } else if ("local".equals(repoAlias)) {
            return new File(project.getBasedir(), "modules");
        } else {
            throw new IllegalArgumentException(
                "Property ceylonRepository must one of 'user', 'cache' or 'local'. Defaults to 'user'");
        }  
    }

    /**
     * Parses a Maven POM file.
     * 
     * @param pomFile The path of the POM file to parse, must not be <code>null</code>.
     * @return The model from the POM file, never <code>null</code>.
     * @throws MojoExecutionException If the POM could not be parsed.
     */
    Model readModel(final File pomFile) throws MojoExecutionException {
        Reader reader = null;
        try {
            reader = ReaderFactory.newXmlReader(pomFile);
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


    /**
     * Installs additional files into the same repo directory as the artifact.
     * 
     * @param installedFile The artifact to which this additional file is related
     * @param fileExt The full file name or extension (begins with .) of the additional file
     * @param payload The String to write to the additional file
     * @param chop True of it replaces the artifact extension, false to attach the extension
     * @throws MojoExecutionException In case of installation error
     */
    void installAdditional(final File installedFile, final String fileExt, final String payload, final boolean chop)
            throws MojoExecutionException {
        File additionalFile = null;
        if (chop) {
            String path = installedFile.getAbsolutePath();
            additionalFile = new File(path.substring(0, path.lastIndexOf('.')) + fileExt);
        } else {
            if (fileExt.indexOf('.') > 0) {
                additionalFile = new File(installedFile.getParentFile(), fileExt);
            } else {
                additionalFile = new File(installedFile.getAbsolutePath() + fileExt);
            }
        }
        getLog().debug("Installing additional file to " + additionalFile);
        try {
            additionalFile.getParentFile().mkdirs();
            FileUtils.fileWrite(additionalFile.getAbsolutePath(), "UTF-8", payload);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to install additional file to " + additionalFile, e);
        }
    }

    /**
     * Sets the 'skip' property from configuration.
     * 
     * @param skipFlag True to skip installation
     */
    public void setSkip(final boolean skipFlag) {
        this.skip = skipFlag;
    }
}