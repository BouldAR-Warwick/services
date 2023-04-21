#!/bin/bash

# if not on server -> do nothing
if [ ! -d ~/tomcat-10.0.27/ ]; then
  echo "Not on server.";
  exit 1
fi

# remove war files from server
rm -rv ~/tomcat-10.0.27/webapps/webservices-1.0-SNAPSHOT
rm -v ~/tomcat-10.0.27/webapps/webservices-1.0-SNAPSHOT.war

# clean and make war
mvn --file ~/Projects/pbrg_services/pom.xml clean package -Dmaven.test.skip
# chmod 755 ~/Projects/pbrg_services/target/webservices-1.0-SNAPSHOT.war

# migrate war to tomcat server
mv ~/Projects/pbrg_services/target/webservices-1.0-SNAPSHOT.war ~/tomcat-10.0.27/webapps/

# run tomcat startup (ensure refresh)
# source ~/tomcat-10.0.27/bin/startup.sh
