# Continuous-Integration

test web hook again againnagain again again again again again again again again
## Installation
Clone the repo and install maven/java and set you java version to 11.0 if not already done so. You can check with the mvn -V command once you've installed maven.

For Ubuntu:
```
//Install Maven
$ sudo apt install maven 

//Install jdk/jre v.11 if 'mvn -v' doesn't return java version 11
$ sudo apt-get install openjdk-11-jdk openjdk-11-jre

// List current installed paths for java and select version 11
$ sudo update-alternatives --config java

//Pick the path from the previous command and update JAVA_HOME global var
$ export JAVA_HOME=/usr/lib/jvm/java-11-oracle
```
For Mac OSX:
```
//Install Maven
$ brew install maven 

//Install jdk/jre v.11 if 'mvn -v' doesn't return java version 11
$ brew install openjdk@11

//For the system Java wrappers to find this JDK, symlink it with
$ sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk

//Update JAVA_HOME global var
$ export JAVA_HOME=`/usr/libexec/java_home -v 11.0`
```

## Build
To build, you need to cd into the my-app folder and run mvn. However the pom.xml settings file for Maven is outside this folder, so it's recommended to use the following command while located in the project root (i.e. DD2480):
```
(cd my-app && mvn assembly:assembly --batch-mode --update-snapshots verify)
```

## Run
To run the project, like above with building, we recommend being in the project root folder and run the following command:
```
(cd my-app/ && java -jar target/my-app-1.0-SNAPSHOT-jar-with-dependencies.jar)
```
