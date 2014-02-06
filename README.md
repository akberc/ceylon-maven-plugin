ceylon-maven-plugin
===================

Maven Plugin for interaction with Ceylon repositories
[![Build Status](https://buildhive.cloudbees.com/job/dgwave/job/ceylon-maven-plugin/badge/icon)](https://buildhive.cloudbees.com/job/dgwave/job/ceylon-maven-plugin/)

- Minimum requirements: Java 7 and Maven 3.0.5

### Build the plugin with
- `mvn clean install`

### Use the plugin with the following snippet in the `pom.xml` of a `jar` project
```xml
<build>
 ...
    <plugins>
     ...
        <plugin>
            <groupId>com.dgwave.car</groupId> 
            <artifactId>ceylon-maven-plugin</artifactId> 
            <version>0.3</version> 
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

- `ceylon:help`
Display help information on ceylon-maven-plugin.
Call `mvn ceylon:help -Ddetail=true -Dgoal=<goal-name>` to display parameter details.