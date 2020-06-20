FROM gradle:jdk11 as build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --warning-mode all

FROM openjdk:11-jdk-slim-buster

WORKDIR /home/itemwarehouse

COPY --from=build /home/gradle/src/build/libs/ItemWarehouse-*-all.jar ItemWarehouse.jar

ENTRYPOINT ["java"]
CMD ["-jar", "ItemWarehouse.jar"]