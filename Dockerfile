FROM jamesdbloom/docker-java7-maven

RUN DEBIAN_FRONTEND=noninteractive apt-get install -yq mysql-server

WORKDIR /var/ana_storm

COPY db.sql /var/ana_storm/db.sql
RUN service mysql start && mysql < db.sql

# Prepare by pre-downloading dependencies
COPY pom.xml /var/ana_storm/pom.xml
COPY src/resources /var/ana_storm/src/resources
RUN mvn dependency:resolve
RUN mvn verify

COPY . /var/ana_storm
RUN mvn package

CMD service mysql start && java -jar target/ana-storm-1.0-SNAPSHOT.jar && mysql facebook_analytics -e "select * from facebook_analytics_service_facebookpage \G"
