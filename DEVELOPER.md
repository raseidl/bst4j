To prepare all releases:
mvn release:clean release:prepare

For Snapshot Releases:
mvn clean deploy

For General Releases:
mvn release:perform

Snapshot releases are essentially done automatically when the version in the POM ends in -SNAPSHOT.

The reason to use the release:clean and release:prepare is to ensure the tags and versions are updated in Github.
This means we are using the maven release plugin as opposed to just the Nexus plugin.
The Nexus plugin updates the maven repos, but not Github.

Read here about what release:prepare does:
http://maven.apache.org/maven-release/maven-release-plugin/examples/prepare-release.html

mvn release:perform takes the last version and deploys it.
mvn deploy takes the most recent (snapshot) version and deploys it.
