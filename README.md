# Continuous-Integration

## Project Description

The project implements a small continuous integration CI server. This CI server has only the core features of continuous integration. The core CI features implemented are the following:

#### Core CI feature #1 - compilation

The CI server supports compiling the project, a static syntax check performed for languages without compiler. Compilation is triggered as webhook, the CI server compiles the branch where the change has been made, as specified in the HTTP payload.

#### Core CI feature #2 - testing

The CI server supports executing the automated tests of the project. Testing is triggered as webhook, on the branch where the change has been made, as specified in the HTTP payload.

#### Core CI feature #3 - notification

The CI server supports notification of CI results by setting commit status. The CI server sets the commit status on the repository.

#### Core CI feature #4 - history

The CI server keeps the history of the past builds. This history persists even if the server is rebooted. Each build is given a unique URL, that is accessible to get the build information (commit identifier, build date, build logs). One URL exists to list all builds.

## Progress of Team Assessment

Achieved state: performing - the team is working effectively and efficiently. The team is working effectively in a cohesive unit, organizing productive work sessions where we code and discuss issues collaboratively. The communication is open - maintaining communication on Discord channels outside the work sessions and otherwise verbally together and continuously addresses problems without outside help. The team is committed to achieve the features listed in the grading criteria - distributing the workload by feature in the form of issues on GitHub. Effective progress is continually being made with little wasted work and back-tracking a result of our open communication. To meet the next state, adjourned, team responsibilities need to be completely fulfilled with no further effort needed. The state can only be achieved after four assignments - which is when we have no further obligation to collaborate or be available since our mission is done.

## Installation

### Versions
* Java: 11.0.21 (OpenJDK)
* Apache Maven: 3.6.3

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

## Add authentication token

One need to manually add the authentication by adding a file '.env' inside the my-app directory with the following contents:

```
AUTH_TOKEN_ENV="<your GitHub personal token with repoistory permission>"
PORT=8016
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


## View browsable JavaDocs

To view browsable JavaDocs, run following command in root:

```
mvn javadoc:javadoc
```

The docs are available in HTML form in the drectory below. One can drag-and-drop into their respective browser to view.

```
Continuous-Integration/my-app/target/site/apidocs/
```

## How to view commit history

To view commit build history, open: http://localhost:8016/

If ngrok server is up and running, use URL provided.


## Statement of Contributions

### Henrik Åkesson

- Pair-programmed notification feature and set Commit Status method with Robert Skoglund.
- Developed unit test for assuring correctness of setCommitStatus method.
- Added environment variables for accessing authentication token (AUTH_TOKEN_ENV) and port number (PORT) to enhance security and configuration flexibility.
- Added code documentation according to best practises to uphold readability.

### Robin Eggestig
- Created the github project - Along with rulesets for disallowing merges/commits to main via the following rules:
  - at least 2 approving reviews.
  - 3 successful checks (Build, Test, and Assemble).
  - No direct commit to main (Must be a pull request).
- Created maven project skeleton.
- Implemented smallest-java-ci with our skeleton.
- Implemented the `cloneRepo` method.
- Implemented the test and assemble methods based on the build method implemented by Anton.
- Implemented test(s) for said implemented code above.
- Debugged and fiddled with configurations, including the pom.xml file.
- Pair-programmed different issues partially.

### Anton Sederlin
- Implemented the base functionality of requirements P1 and P2, i.e. enuring that the CI server compile, build and test the branch where the most recent commit was made, the commit that triggered the webHook (current push event).
- Implemented logic for receiving and parsing the incoming webHook payload.
- Created tests for the auto-build/test methods.
- Performed extensive debugging and refractoring. 

### Tsz Ho Wat
- Pair-programmed in keep commit history with Anton
- Create ReserveHistory.java, it can list all the history commit with a url and each commit have a unique url show the commit detail,this history persists even if the server is rebooted.
- Create ReserveHistoryTest.java, it test all the main function in ReserveHistory.java
- Add documentation in all public function for ReserveHistory.java
- Conducted reviews and helped merge the program

### Robert Skoglund
- Pair-programmed notification feature and setCommit function with Henrik
- Extensive debugging to read authentication token successfully, including dependency management
- Developed unit tests for successful reading of authentication as well as changes to commit status unit test
- Completed progress of team assessment with regards to states checklist
