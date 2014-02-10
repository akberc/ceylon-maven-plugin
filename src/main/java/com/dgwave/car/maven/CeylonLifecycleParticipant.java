package com.dgwave.car.maven;

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.settings.Profile;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import com.dgwave.car.repo.CeylonUtil;

/**
 * If the Ceylon plugin is included as an extension, it will try to set up SDK.
 * 
 * @author Akber Choudhry
 * 
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "ceylon")
public class CeylonLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    /**
     * Runtime information.
     */
    @Requirement
    private RuntimeInformation runtime;

    /**
     * Logger.
     */
    @Requirement
    private Logger logger;

    /**
     * Interception after projects are known.
     * @param session The Maven session
     * @throws MavenExecutionException In case of error
     */
    @Override
    public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
        
        if (session.getGoals().contains("help")
            || session.getGoals().contains("ceylon:help")
            || session.getGoals().contains("ceylon:sdk-check")) {

            return;
        }
        
        for (MavenProject project : session.getProjects()) {
            if (project.getPlugin("ceylon") != null
                || "car".equals(project.getArtifact().getArtifactHandler().getPackaging())
                || usesCeylonRepo(project)) {

                logger.info("At least one project is using the Ceylon plugin. Checking for SDK");

                String ceylonRepo = System.getProperty("ceylon.repo");

                if (ceylonRepo == null) {
                    List<String> activeProfiles = session.getSettings().getActiveProfiles();
                    for (String profile : activeProfiles) {
                        Profile p = session.getSettings().getProfilesAsMap().get(profile);
                        if (p != null && p.getProperties() != null 
                                && p.getProperties().get(CeylonUtil.CEYLON_REPO) != null) {
                            logger.info("Property ceylon.repo found in Maven settings.xml in profile " + profile); 
                            System.setProperty(CeylonUtil.CEYLON_REPO, 
                                (String) p.getProperties().get(CeylonUtil.CEYLON_REPO));
                            return;
                        }
                    }
                    
                    // last resort
                    CeylonSdkCheck mojo = new CeylonSdkCheck();

                    try {
                        mojo.execute();
                    } catch (MojoExecutionException e) {
                        throw new MavenExecutionException("At least one project uses the Ceylon Maven plugin.", e);
                    }
                }
            }
        }
    }

    /**
     * Checks that a project use the Ceylon Maven plugin.
     * @param project Project
     * @return true if the Ceylon plugin is used, false if not used
     */
    private boolean usesCeylonRepo(final MavenProject project) {
        for (Repository repo : project.getRepositories()) {
            if ("ceylon".equals(repo.getLayout())) {
                return true;
            }
        }

        for (Artifact ext : project.getExtensionArtifacts()) {
            if ("ceylon-maven-plugin".equals(ext.getArtifactId())) {
                return true;
            }
        }
        return false;
    }
}