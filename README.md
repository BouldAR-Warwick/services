# Web services for project pbrg

[![Java CI with Maven](https://github.com/herobrinor/pbrg_services/actions/workflows/maven.yml/badge.svg)](https://github.com/herobrinor/pbrg_services/actions/workflows/maven.yml)
[![GitHub Super-Linter](https://github.com/herobrinor/pbrg_services/workflows/Lint%20Code%20Base/badge.svg)](https://github.com/marketplace/actions/super-linter)

## compile commands
```bash
mvn clean package
```
Then copy the war file in generated target dir to tomcat webapps dir.

## login to database and apply schema
Connect from grabourg:
```bash
mysql -h mysql -D grabourgdb -u grabourg -p
```
Apply schema command:
```bash
source ~/Database/schema.sql
```
