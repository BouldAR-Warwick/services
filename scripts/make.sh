#!/bin/bash

# if not on server -> do nothing
if [ ! -d ~/tomcat-10.0.27/ ]; then
  echo "Not on server.";
  exit 1
fi

# remove war files from server
rm -r ~/tomcat-10.0.27/webapps/webservices-1.0-SNAPSHOT
rm ~/tomcat-10.0.27/webapps/webservices-1.0-SNAPSHOT.war

# clean and make war
cd ~/Projects/pbrg_services/;
mvn clean package;
chmod 700 ~/Projects/pbrg_services/target/webservices-1.0-SNAPSHOT.war

mv ~/Projects/pbrg_services/target/webservices-1.0-SNAPSHOT.war ~/tomcat-10.0.27/webapps/
