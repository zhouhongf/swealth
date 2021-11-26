FROM anapsix/alpine-java
VOLUME /tmp
ADD swealth-0.0.1.jar app.jar
EXPOSE 7070
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
