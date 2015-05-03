package com.dgwave.car.maven;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.enforcer.AbstractVersionEnforcer;

public class RequireCeylonVersion extends AbstractVersionEnforcer {
	
	@Override
	public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
		Log log = helper.getLog();
		enforceVersion(log, "Ceylon", getVersion(), 
				new DefaultArtifactVersion("1.1.0"));
	}
}
/*// get the various expressions out of the helper.
MavenProject project = (MavenProject) helper.evaluate( "${project}" );
MavenSession session = (MavenSession) helper.evaluate( "${session}" );
String target = (String) helper.evaluate( "${project.build.directory}" );
String artifactId = (String) helper.evaluate( "${project.artifactId}" );

// retrieve any component out of the session directly
ArtifactResolver resolver = (ArtifactResolver) helper.getComponent( ArtifactResolver.class );
RuntimeInformation rti = (RuntimeInformation) helper.getComponent( RuntimeInformation.class );*/
