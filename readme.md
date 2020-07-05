We will be doing all of our coding in the class java.com.vgrazi.study.completablefuture.CompletableFutureApplicationTests, so be sure to have that open.

This project is configured for Java 11. If you wish to use it for earlier versions of Java (Java 9 is the earliest allowable), then change the source and target for the maven-compiler-plugin in the pom.xml from 11 to 9.

Then run a `mvn clean verify` from your IDE. You should see something like this:  
`c.v.s.c.EnvironmentValidator             : You're good to go!`

If you get an error  
`"invalid target release: 11 -> [Help 1] "`   
then set your PATH to include 
 %JAVA_HOME%\bin for Windows, or
$JAVA_HOME/bin otherwise

For example, on my Windows 10 machine I have:
set PATH="c:\Program Files\Java\jdk-11.0.3\bin";%PATH%

To verify your Java version, do
`java -version' 

Then try again
 `mvn clean verify` from your IDE. You should see something like this:  
`c.v.s.c.EnvironmentValidator             : You're good to go!`

If you still can't see it, just follow along, we will be coding everything live
