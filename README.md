# Aeronautic Product Release API (APRA) 

APRA provides the private sector greater access to core FAA data to spur more innovation in the aviation industry, 
and ultimately advance the FAA's mission of safety in flight.  Based on stakeholder recommendations, this 
initiative's first effort strived to provide both more data and better data for Aeronautical products and 
information. Solution Delivery developed and deployed web services for several aeronautical products including both
information about those products and the ability to automatically download those products which satisfied one of 
the recommendations.

## About APRA

APRA provides a mechanism for a client program to scan the FAA public web site for a specific type of aeronautic 
product for download. The aeronautic products available include charts in various formats. Such charts are often
released to the public on either a 28 day or 56 day airspace cycle. Upon release of charts, the URL(s) for
the chart retrieval necessarily change. APRA constructs the standard URL where the chart may be downloaded 
to enable a software application to pull the charts without the need for a human interface or web based GUI.

## Prerequisites

- **Java 8** (JDK 1.8) or later
- **Apache Maven** 3.x
- Access to the FAA internal Artifactory repository for the `apraresponse` dependency

## Building and Running

```bash
# Build the project
mvn clean package

# Run tests
mvn clean test
```

The application is packaged as a WAR file and can be deployed to any Java EE servlet container (e.g., Apache Tomcat).

Configuration properties are loaded from `/opt/apra/conf/config.properties`. If this file is not present, default configuration values are used.

## Documentation

Documentation for the API may be found on swaggerhub
https://app.swaggerhub.com/apis/FAA/APRA
