package com.dgwave.car.maven;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;

@Component(role = ComponentConfigurator.class, hint = "car")
public class CeylonPackagingConfig extends AbstractComponentConfigurator {

}
