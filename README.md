# Maven build  
`~/java_env/maven/bin/mvn archetype:generate -DgroupId=tech.cybersword -DartifactId=tech.cybersword.images -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`  
# build  
`~/java_env/maven/bin/mvn clean package`  
# run  
`~/java_env/jdk/Contents/Home/bin/java -jar target/tech.cybersword.images-1.0-SNAPSHOT.jar`  

`~/java_env/maven/bin/mvn clean package | ~/java_env/jdk/Contents/Home/bin/java -jar target/tech.cybersword.images-1.0-SNAPSHOT.jar pentest1234 /Users/David/sandbox/pics/ /Users/David/git/tech.cybersword.images/ payloads false true false`  

# scp  
`scp /lokaler/pfad/ benutzer@192.168.1.5:/home/benutzer/dokument.txt`  

# show last bytes from files  
`tail -c 40 *`  

# payloads  

- https://github.com/payloadbox/command-injection-payload-list
- https://github.com/payloadbox/xss-payload-list
- 