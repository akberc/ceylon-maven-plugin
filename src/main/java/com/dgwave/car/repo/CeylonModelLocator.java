package com.dgwave.car.repo;

import java.io.File;

import org.apache.maven.model.locator.ModelLocator;
import org.codehaus.plexus.component.annotations.Component;

/**
 * This class locates the Ceylon module model from various sources.
 * @author Akber Choudhry
 */
@Component(role = ModelLocator.class, hint = "ceylon")
public class CeylonModelLocator implements ModelLocator {

    /**
     * Implementing Maven API to locate the project model.
     * @param folder The folder in which to look
     * @return File The file that represents the POM. It may be a Ceylon properties or .module file
     */
    public File locatePom(final File folder) {
        if (folder != null && folder.exists() && folder.isDirectory()) {
            String moduleVersion = folder.getParentFile().getName();
            File[] list = folder.listFiles();
            File[] three = new File[CeylonUtil.NUM_CEYLON_JAVA_DEP_TYPES];
            
            for (File file : list) {
                if (file.isFile()) {
                    if ("module.xml".equals(file.getName())) {
                        three[0] = file;
                    } else if (file.getName().endsWith("-" + moduleVersion + ".module")) {
                        three[1] = file;
                    } else if ("module.properties".equals(file.getName())) {
                        three[2] = file;
                    }
                }
            }
            
            for (File file : three) {
                if (file != null) {
                    return file;
                }
            }
        }
        
        return new File(folder.getPath() + File.pathSeparator + "pom.xml"); // dummy
    }
}
