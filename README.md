# Netflix Contributions to Spring Cloud

[![Build Status](https://travis-ci.org/netflix-spring-one/spring-cloud-netflix-contrib.svg?branch=master)](https://travis-ci.org/netflix-spring-one/spring-cloud-netflix-contrib)
[![Apache 2.0](https://img.shields.io/github/license/nebula-plugins/gradle-rxjava-project-plugin.svg)](http://www.apache.org/licenses/LICENSE-2.0)

This project is a temporary repository of contributions by Netflix to Spring Cloud that have not yet been incorporated.

* Spectator metrics collection, including metrics collection for Spring MVC request mappings, RestTemplate calls, and Spring Integration messages.
* Publication of Spectator/Servo metrics to the Atlas metrics backend.
* Enhancements to Eureka integration.

# Installation

To use, add the following Gradle dependency:

```
dependency {
   compile 'com.netflix.spring:spring-cloud-netflix-contrib:0.1.0'
}
```
