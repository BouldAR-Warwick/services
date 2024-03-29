#!/bin/bash

# if not on server -> do nothing
if [ ! -d ~/tomcat-10.0.27/ ]; then
  echo "Not on server.";
  exit 1
fi

# remove war files from server
rm -rv ~/tomcat-10.0.27/webapps/webservices-1.0
rm -v ~/tomcat-10.0.27/webapps/webservices-1.0.war

# clean and make war
mvn --file ~/Projects/services/pom.xml clean package -Dmaven.test.skip
# chmod 755 ~/Projects/services/target/webservices-1.0.war

# migrate war to tomcat server
mv ~/Projects/services/target/webservices-1.0.war ~/tomcat-10.0.27/webapps/

# run tomcat startup (ensure refresh)
# source ~/tomcat-10.0.27/bin/startup.sh
