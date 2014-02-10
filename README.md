ceylon-maven-plugin
===================

Maven Plugin for interaction with Ceylon repositories
[![Build Status](https://buildhive.cloudbees.com/job/dgwave/job/ceylon-maven-plugin/badge/icon)](https://buildhive.cloudbees.com/job/dgwave/job/ceylon-maven-plugin/)

- Minimum requirements: Java 7 and Maven 3.0.5

### Plugin is in Maven Central repository. Use the plugin with the following snippet in the `pom.xml` of a `jar` project
```xml
<build>
 ...
    <plugins>
     ...
        <plugin>
            <groupId>com.dgwave.car</groupId> 
            <artifactId>ceylon-maven-plugin</artifactId> 
            <version>0.3</version>
            <extensions>true</extensions>
        </plugin>
     ...
    </plugins>
 ...
</build>
```

### Maven goals are
- `ceylon:install`
From within a project, installs packaged jar artifacts into the Ceylon `user` repository.
Target can be changed to 'cache' or 'local'. Reactor projects (modules) are supported.
Module dependencies set according to the Maven project model.

- `ceylon:install-jar`
Project context not required. use the `-Dfile` parameter to point to a jar file.  A `pom.xml`
file in the same directory or within the jar file will be parsed for dependencies. Installs into the 
Ceylon 'user' repository. Target can be changed to `cache` or `local`.

- `ceylon:sdk-check`
Checks for the presence of the Ceylon system repo, which can be configured through:
 - System property `ceylon.repo`
 - Property `ceylon.repo` in an active profile in Maven `settings.xml`
 - `repo` folder under the path specifiied in `CEYLON_HOME` environment variable

- `ceylon:sdk-download`
Downloads the SDK to a location defined in the `-Dceylon.sdk.downloadTo` property.
Path defaults to the `.ceylon` directory under the user's home directory. The download
URL points to the Ceylon 1.0.0 release, but can be changed by the `ceylon.sdk.fromURL` property.

- `ceylon:help`
Display help information on ceylon-maven-plugin.
Call `mvn ceylon:help -Ddetail=true -Dgoal=<goal-name>` to display parameter details.

### Build the Ceylon Maven plugin with
- `mvn clean install`