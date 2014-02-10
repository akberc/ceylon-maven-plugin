package com.dgwave.car.maven;

import org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Provides Ceylon packaging 'car'.
 * @author Akber Choudhry
 */

@Component(role = LifecycleMapping.class, hint = "car", 
    configurator = "com.dgwave.car.maven.CeylonPackagingConfig")
public class CeylonPackaging extends DefaultLifecycleMapping {
    

    
}