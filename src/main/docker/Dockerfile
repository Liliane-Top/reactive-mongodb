FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR application
ADD maven/reactive-mongodb-0.0.1-SNAPSHOT.jar ./
RUN java -Djarmode=layertools -jar ${project.build.finalName}.jar extract

FROM eclipse-temurin:17-jdk-jammy

WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.launch.JarLauncher"]