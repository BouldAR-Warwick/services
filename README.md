# Services - PBRG (Problem Board Route Generator)

[![Java CI with Maven](https://github.com/tmcowley/pbrg-services/actions/workflows/maven.yml/badge.svg)](https://github.com/tmcowley/pbrg-services/actions/workflows/maven.yml)
[![GitHub Super-Linter](https://github.com/tmcowley/pbrg-services/workflows/Lint%20Code%20Base/badge.svg)](https://github.com/marketplace/actions/super-linter)

## Technologies
- Apache Tomcat
- Java 11
- MySQL

## Build and integrate `.war` file into Apache Tomcat. 
```bash
source scripts/make.sh
```

### compile commands
```bash
mvn clean package
```

### login to database and apply schema
Connect:
```bash
mysql -h mysql -D grabourgdb -u grabourg -p
```
Apply schema command:
```bash
source ~/Database/schema.sql
```
