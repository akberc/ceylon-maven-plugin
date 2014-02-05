package com.dgwave.car.common;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Module Representation.
 * @author Akber Choudhry (Adapted from Ceylon Module Resolver)
 *
 */
public class Module {
    
    /**
     * This module's identifier.
     */
    private ModuleIdentifier module;
    
    /**
     * This module's dependencies.
     */
    private Set<ModuleIdentifier> dependencies = new LinkedHashSet<ModuleIdentifier>();

    /**
     * Default constructor.
     * @param mi The name/version of this module
     */
    public Module(final ModuleIdentifier mi) {
        this.module = mi;
    }

    /**
     * Add a dependency.
     * @param mi The name/version of the dependency to ad
     */
    public void addDependency(final ModuleIdentifier mi) {
        dependencies.add(mi);
    }

    /**
     * Return this module's identifier.
     * @return ModuleIdentifier this module's name/version
     */
    public ModuleIdentifier getModule() {
        return module;
    }

    /**
     * This module's ordered dependencies.
     * @return Set of module identifies
     */
    public Set<ModuleIdentifier> getDependencies() {
        return dependencies;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Module module1 = (Module) o;

        if (!module.equals(module1.module)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return module.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("\nModule: " + module + "\n");
        builder.append("Dependencies: ").append(dependencies.size()).append("\n");
        for (ModuleIdentifier dep : dependencies) {
            builder.append("\t").append(dep).append("\n");
        }
        return builder.toString();
    }
}