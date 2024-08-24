FROM openjdk:17
COPY ./build/libs/HRImport-0.0.1-SNAPSHOT.jar /usr/app/
WORKDIR /usr/app/
RUN sh -c "touch HRImport-0.0.1-SNAPSHOT.jar"
ENTRYPOINT ["java", "-jar", "HRImport-0.0.1-SNAPSHOT.jar"]