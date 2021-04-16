FROM adoptopenjdk:14-jre-hotspot
RUN mkdir /opt/batch-processor \
    && mkdir /opt/batch-processor/data \
    && mkdir /opt/batch-processor/data/in\
    && mkdir /opt/batch-processor/data/out\
    && mkdir /opt/batch-processor/data/error\
    && mkdir /opt/batch-processor/data/archive
COPY ./target/batch-processor-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/batch-processor
WORKDIR /opt/batch-processor
CMD ["java", "-jar", "batch-processor-1.0-SNAPSHOT-jar-with-dependencies.jar"]