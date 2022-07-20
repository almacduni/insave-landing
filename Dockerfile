#Build stage
FROM maven:3.6.3-jdk-11-slim AS build
COPY src /home/app/src
COPY lombok.config /home/app
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /opt/workdir/
COPY --from=build /home/app/target/IN10-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar

ARG CERT="ca-certificate.crt"
#import cert into java
COPY $CERT /opt/workdir/
RUN keytool -importcert -file $CERT -alias $CERT -cacerts -storepass changeit -noprompt

ENV JAVA_TOOL_OPTIONS "-Xmx1024m -Xms1024m"

EXPOSE 8080
ENTRYPOINT ["sh", "-c","java ${JAVA_TOOL_OPTIONS} -jar /usr/local/lib/demo.jar"]