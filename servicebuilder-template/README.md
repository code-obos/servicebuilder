#
To build archetype:

mvn archetype:create-from-project -Darchetype.properties=archetype.properties
cd target/generated-source/archetype && mvn install


To generate project:


mvn archetype:generate -DarchetypeGroupId=no.obos.util.template -DarchetypeArtifactId=servicebuilder-template-archetype -DarchetypeVersion=8.0.0-SNAPSHOT
