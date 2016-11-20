http://central.sonatype.org/pages/apache-maven.html
For Snapshots:
mvn clean deploy

For Releases:
Remove -SNAPSHOT from tag
mvn clean deploy -P release