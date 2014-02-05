package com.dgwave.car.common;

/**
 * A module identifier: name/version.
 * @author Akber Choudhry (adapted from Ceylon Module Resolver)
 */
public class ModuleIdentifier implements Comparable<ModuleIdentifier> {
    
    /**
     * Module name.
     */
    private String name;
    
    /**
     * Module version.
     */
    private String version;
    
    /**
     * If dependency, is module optional?
     */
    private boolean optional;
    
    /**
     * If dependency, is module to be exported?
     */
    private boolean export;

    /**
     * Default constructor.
     * @param n Module name
     * @param v Module version
     * @param o Is the module an optional dependency?
     * @param e Is the module exported as a dependency?
     */
    public ModuleIdentifier(final String n, final String v, final boolean o, final boolean e) {
        this.name = n;
        if (v == null || v.length() == 0) {
            this.version = "main";
        } else {
            this.version = v;          
        }
        this.optional = o;
        this.export = e;
    }

    /**
     * Creates a ModuleIdentifier from name:version.
     * @param string The name:version string
     * @return ModuleIdentifier The module identifier
     */
    public static ModuleIdentifier create(final String string) {
        String[] split = string.split(":");
        String version = null;
        if (split.length > 1) {
            version = split[1];
        }
        return new ModuleIdentifier(split[0], version, false, false);
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return optional
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @return export
     */
    public boolean isExport() {
        return export;
    }

    @Override
    public int compareTo(final ModuleIdentifier o) {
        int diff = name.compareTo(o.getName());
        if (diff != 0) {
            return diff;
        }
        return version.compareTo(o.getVersion());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleIdentifier that = (ModuleIdentifier) o;

        if (!name.equals(that.name)) {
            return false;
        }
        if (!version.equals(that.version)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }
}
