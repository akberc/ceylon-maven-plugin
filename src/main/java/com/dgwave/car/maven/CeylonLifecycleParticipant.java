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

import com.dgwave.car.common.CeylonUtil;

/**
 * If the Ceylon plugin is used or should be used, tries to find the SDK.
 * 
 * @author Akber Choudhry
 * 
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "ceylon")
public class CeylonLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    @Requirement
    private Logger logger;
    
    /**
     * Runtime information.
     */
    @Requirement
    private RuntimeInformation runtime;

    private void findCeylonRepo(MavenSession session) throws MavenExecutionException {
        String ceylonRepo = System.getProperty("ceylon.repo");
        // check from settings
        if (ceylonRepo == null) {
            List<String> activeProfiles = session.getSettings().getActiveProfiles();
            for (String profile : activeProfiles) {
                Profile p = session.getSettings().getProfilesAsMap().get(profile);
                if (p != null && p.getProperties() != null 
                        && p.getProperties().get(CeylonUtil.CEYLON_REPO) != null) {
                    logger.info("Property ceylon.repo found in Maven settings.xml in profile " + profile);
                    ceylonRepo = (String) p.getProperties().get(CeylonUtil.CEYLON_REPO);
                    System.setProperty(CeylonUtil.CEYLON_REPO, ceylonRepo);
                    return;
                }
            }
        }
        
        if (ceylonRepo == null) {
            // last resort
            CeylonSdkCheck mojo = new CeylonSdkCheck();
            
            try {
                mojo.execute();
                ceylonRepo = System.getProperty(CeylonUtil.CEYLON_REPO);
            } catch (MojoExecutionException e) {
                throw new MavenExecutionException("Ceylon Maven plugin enabled but: ", e);
            }
        }
    };
    
    /**
     * Interception after projects are known.
     * @param session The Maven session
     * @throws MavenExecutionException In case of error
     */
    @Override
    public void afterProjectsRead(final MavenSession session) throws MavenExecutionException {
        boolean anyProject = false;
        for (MavenProject project : session.getProjects()) {
            if (project.getPlugin("ceylon") != null
            	|| project.getPlugin("ceylon-maven-plugin") != null
            	|| "ceylon".equals(project.getArtifact().getArtifactHandler().getPackaging())
                || "car".equals(project.getArtifact().getArtifactHandler().getPackaging())
                || "ceylon-jar".equals(project.getArtifact().getArtifactHandler().getPackaging())
                || usesCeylonRepo(project)) {
            	anyProject = true;
            }
        }
        if (anyProject) {
        	logger.info("At least one project is using the Ceylon plugin. Preparing.");
        	findCeylonRepo(session);
        }
        logger.info("Adding Ceylon repositories to build");
        session.getRequest().setWorkspaceReader(
        		new CeylonWorkspaceReader(
        				session.getRequest().getWorkspaceReader(),
        				logger));
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
        
        for (Artifact ext : project.getPluginArtifacts()) {
            if (ext.getArtifactId().startsWith("ceylon")) {
                return true;
            }
        }
        return false;
    }
}