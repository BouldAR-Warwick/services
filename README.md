<p style="text-align: center"><a href="https://github.com/BouldAR-Warwick"><img alt="BouldAR Logo" src="https://github.com/BouldAR-Warwick/bouldar-assets/raw/main/logo_purple.png" width="512"/></a></p>

# Services

| __Build Status__  | [![Java CI with Maven](https://github.com/tmcowley/pbrg-services/actions/workflows/maven.yml/badge.svg)](https://github.com/tmcowley/pbrg-services/actions/workflows/maven.yml) |
|:------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| __Code Quality__  | [![GitHub Super-Linter](https://github.com/tmcowley/pbrg-services/workflows/Lint%20Code%20Base/badge.svg)](https://github.com/marketplace/actions/super-linter) |
| __Security Assessment__ | [![CodeQL](https://github.com/BouldAR-Warwick/services/workflows/SAST:%20CodeQL/badge.svg)](https://codeql.github.com/) |
| __Test Coverage__ | ![Coverage](.github/badges/jacoco.svg)                                                                                                                                          |

## Technologies
- Apache Tomcat v10.0.27
- Java 11
- MySQL v5.5.68-MariaDB

## Build and integrate `.war` file into Apache Tomcat
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
