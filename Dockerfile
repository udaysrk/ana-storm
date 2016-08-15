FROM jamesdbloom/docker-java7-maven

RUN apt-get update && DEBIAN_FRONTEND=noninteractive

WORKDIR /var/ana_storm

# Prepare by pre-downloading dependencies
COPY pom.xml /var/ana_storm/pom.xml
COPY src/resources /var/ana_storm/src/resources
RUN mvn dependency:resolve
RUN mvn verify

COPY . /var/ana_storm
RUN mvn package -e

CMD java -jar target/ana-storm-1.0-SNAPSHOT.jar
