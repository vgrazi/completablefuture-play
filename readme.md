This is configured for Java 11. If you wish to use it for earlier versions of Java (Java 9 is the earliest allowable), then change the source and target for the maven-compiler-plugin in the pom.xml from 11 to 9.

Then run a `mvn clean verify` from your IDE. You should see something like this:  
`c.v.s.c.EnvironmentValidator             : You're good to go!`
