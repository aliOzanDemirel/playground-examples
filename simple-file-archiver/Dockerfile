FROM maven:3.5.4-jdk-8-alpine

EXPOSE 8080 8443

WORKDIR /app
COPY . /app

RUN mvn -e clean install spring-boot:repackage
RUN mkdir -p /workspace-link/UploadedFiles
RUN mkdir /workspace-link/logs

CMD ["java", "-Dspring.profiles.active=docker", "-jar", "target/archiver.jar"]
