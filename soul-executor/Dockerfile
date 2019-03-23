FROM java:openjdk-8-jre-alpine
VOLUME /tmp
ADD target/soul-bootstrap.jar soul-bootstrap.jar
EXPOSE 9800
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/soul-executor.jar"]
