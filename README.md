# Continuous-Integration

## Progress of Team Assessment

Achieved state: performing - the team is working effectively and efficiently. The team is working effectively in a cohesive unit, organizing productive work sessions where we code and discuss issues collaboratively. The communication is open - maintaining communication on Discord channels outside the work sessions and otherwise verbally together and continuously addresses problems without outside help. The team is committed to achieve the features listed in the grading criteria - distributing the workload by feature in the form of issues on GitHub. Effective progress is continually being made with little wasted work and back-tracking a result of our open communication. To meet the next state, adjourned, team responsibilities need to be completely fulfilled with no further effort needed. The state can only be achieved after four assignments - which is when we have no further obligation to collaborate or be available since our mission is done.

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

### issue#14

#### GitHub CLI installation
brew install gh


## Statement of Contributions

### Henrik Åkesson

### Robin Eggestig

### Anton Sederlin

### Tsz Ho Wat

### Robert Skoglund
- Pair-programmed notification feature and setCommit function with Henrik
- Extensive debugging to read authentication token successfully, including dependency management
- Developed unit tests for successful reading of authentication as well as changes to commit status unit test
- Completed progress of team assessment with regards to states checklist