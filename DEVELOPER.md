# Releases
To prepare all releases:
mvn release:clean release:prepare

For Snapshot Releases:
mvn -Drelease=true clean deploy

For General Releases:
Use the same command as above, but first remove the SNAPSHOT from pom.xml

The property "-Drelease=true" is so that the CI does not need GPG keys for its builds.
Instead, they are in their own profile.

Snapshot releases are essentially done automatically when the version in the POM ends in -SNAPSHOT.

The reason to use the release:clean and release:prepare is to ensure the tags and versions are updated in Github.
This means we are using the maven release plugin as opposed to just the Nexus plugin.
The Nexus plugin updates the maven repos, but not Github.

Read here about what release:prepare does:
http://maven.apache.org/maven-release/maven-release-plugin/examples/prepare-release.html

mvn release:perform takes the last version and deploys it.
mvn deploy takes the most recent (snapshot) version and deploys it.

## Verify Releases
For public releases, you can check here to see the release worked correctly:  
https://oss.sonatype.org/content/repositories/releases/tools/bespoken/bst4j/

It usually replicates to maven in about an hour (or so...still figuring this out). You can check here to see it:  
https://repo1.maven.org/maven2/tools/bespoken/bst4j/

The documentation will show up here:  
https://www.javadoc.io/doc/tools.bespoken/bst4j/

This should appear within 24-hours according to the website.

## Travis and Code Coverage
Code coverage is from codecov.