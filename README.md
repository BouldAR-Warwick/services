# Web services for project pbrg

## compile commands:
mvn clean<br/>
mvn package<br/>
<br/>
Then copy the war file in generated target dir to tomcat webapps dir 

## login to database and apply schema:
Connect from grabou@magpie:<br/>
mysql -h mysql -D grabourgdb -u grabourg -p<br/>
<br/>
Apply schema command:<br/>
source ~/Database/schema.sql<br/>
