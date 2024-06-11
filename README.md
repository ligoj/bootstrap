## :link: Ligoj Bootstrap [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.ligoj.bootstrap/root/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.ligoj.bootstrap/root)
REST+Front-End template with a ton of integrated component with many enterprise features : RBAC, Cache, modular modules

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.ligoj.bootstrap%3Aroot&metric=coverage)](https://sonarcloud.io/component_measures/metric/coverage/list?id=org.ligoj.bootstrap%3Aroot)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?metric=alert_status&project=org.ligoj.bootstrap:root)](https://sonarcloud.io/dashboard/index/org.ligoj.bootstrap:root)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e6c472b13c5a49b4882d27632f79b6de)](https://www.codacy.com/gh/ligoj/bootstrap?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ligoj/bootstrap&amp;utm_campaign=Badge_Grade)
[![CodeFactor](https://www.codefactor.io/repository/github/ligoj/bootstrap/badge)](https://www.codefactor.io/repository/github/ligoj/bootstrap)
[![Known Vulnerabilities](https://snyk.io/test/github/ligoj/bootstrap/badge.svg)](https://snyk.io/test/github/ligoj/bootstrap)
[![Maintainability](https://api.codeclimate.com/v1/badges/f5654026ebe3ab16526c/maintainability)](https://codeclimate.com/github/ligoj/bootstrap/maintainability)
[![License](http://img.shields.io/:license-mit-blue.svg)](http://fabdouglas.mit-license.org/)

Key features:
- Convention over Code for everything: light webpack, http error code, CDI, Java9 named parameters
- RBAC with URL based authorization and dynamic roles
- Exception to REST/HTTP code mapping
- Advanced RS validation
- Tuned Jackson configuration for minified payload and validation
- Test powered with CSV data load to/from JPA entities
- TDD ready with pre-built asserts with Mockito and Wiremock
- Optional encrypted properties support and database configuration with Jasypt
- Tuned configuration for dev, build and production
- Spring-Data extensions for performance, minimal code and exception handling

A Spring based REST architecture
- Spring Boot/Security/Web/Data
- CXF
- JPA / Hibernate
- Hibernate Validator for JPA and RS
- JUnit
- Wiremock

Requirements
- Java-21
- Maven 3.9

# Verify new version

```bash
mvn versions:display-dependency-updates -Pjacoco -Dmaven.version.ignore="^(.*[.-](alpha|beta|rc|M|B|Alpha|Beta|BETA|RC|pre)-?[0-9]*|[0-9]{8}.*)$"
```

# List dependencies of modules

```bash
mvn dependency:tree
```
