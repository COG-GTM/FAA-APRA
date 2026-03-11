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

- **Java 8 (JDK 1.8)** or higher
- **Apache Maven 3.x**
- Access to the FAA internal Artifactory server for the `apraresponse` dependency
- Access to Denodo data services for runtime chart cycle data

## Setup and Build

1. Clone the repository:
   ```bash
   git clone https://github.com/COG-GTM/FAA-APRA.git
   cd FAA-APRA
   ```

2. Ensure Java 8 JDK is installed and `JAVA_HOME` is set:
   ```bash
   java -version   # Should show 1.8.x
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the tests:
   ```bash
   mvn test
   ```

5. The build produces a WAR file (`apra.war`) in the `target/` directory that can be deployed to a servlet container such as Apache Tomcat.

## Running the Application

Deploy the generated `apra.war` to a Java EE servlet container (e.g., Apache Tomcat 8+). The application requires:
- Network access to Denodo data services for chart cycle information
- A configuration file at `/opt/apra/conf/config.properties` (falls back to defaults if not present)

## Java 8 Upgrade Notes

This project has been upgraded from Java 7 to Java 8. Key changes include:

- **Build Configuration**: Updated `maven-compiler-plugin` to version 3.11.0 with source/target 1.8
- **`java.nio.charset.StandardCharsets`**: Replaced `com.google.common.base.Charsets` with `StandardCharsets`
- **`java.time` API**: Replaced `GregorianCalendar`/`Calendar` with `java.time.LocalDate`, `ZonedDateTime`, and `ZoneId` where applicable
- **Streams API**: Replaced traditional `for` loops with `Arrays.stream()`, `IntStream`, `filter()`, `findFirst()`, and `anyMatch()`
- **Lambda Expressions**: Applied lambdas for stream operations and functional-style processing
- **Multi-catch Blocks**: Combined multiple catch blocks with identical handling using multi-catch syntax
- **Try-with-resources**: Applied to `FileInputStream` in configuration loading for automatic resource management
- **Enhanced For Loops**: Replaced index-based for loops with enhanced for-each loops where streams were not appropriate

## Documentation

Documentation for the API may be found on swaggerhub
https://app.swaggerhub.com/apis/FAA/APRA
