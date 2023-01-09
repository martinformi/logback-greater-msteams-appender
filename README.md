# Logback Greater MS Teams appender

Logback appender based on https://github.com/michl-b/logback-msteams-appender with new features coming, fixes and updated 
dependecies.

# How to set up

Prerequisities:
- webhook URL from MS Teams

## Maven

Add the following settings to you pom.xml.
```xml
    <dependency>
      <groupId>com.develmagic</groupId>
      <artifactId>logback-greater-msteams-appender</artifactId>
      <version>1.0.0</version>
    </dependency>

  <repositories>
    <repository>
      <snapshots>
        <enabled>false</enabled>
      </snapshots>
      <id>jfrog</id>
      <name>jfrog</name>
      <url>https://martinformi.jfrog.io/artifactory</url>
    </repository>
  </repositories>
```

## Gradle

Add the following settings to your build.gradle

```groovy
aaa
```

## Gradle (Kotlin)

```kotlin
aaa
```

Add MsTeamsAppender configuration to logback.xml or logback-spring.xml file. Replace the **webhook URL**.

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
  ...

  <appender name="MSTEAMS" class="com.develmagic.logback.MsTeamsAppender">
    <appName>AppName</appName>
    <webhookUri>your web hook from ms teams</webhookUri>
  </appender>
  
  <appender name="ASYNC_MSTEAMS" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="MSTEAMS" />
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
      <level>WARN</level>
    </filter>
  </appender>

  <root level="INFO">
    <appender-ref ref="ASYNC_MSTEAMS" />
  </root>

</configuration>
```

# Build a release
Publish artifact in a new version to JFrog.

```bash
mvn release:prepare
mvn release:perform
```