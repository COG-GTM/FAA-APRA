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
- Access to the FAA internal Artifactory repository (for the `apraresponse` dependency)

## Setup and Build

1. Clone the repository:
   ```bash
   git clone https://github.com/COG-GTM/FAA-APRA.git
   cd FAA-APRA
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the tests:
   ```bash
   mvn test
   ```

4. Package the WAR file for deployment:
   ```bash
   mvn package
   ```
   The WAR file will be generated in the `target/` directory.

## Java 8 Upgrade Notes

This project has been upgraded from Java 7 to Java 8. Key changes include:

- **Build configuration**: Maven Compiler Plugin updated to 3.8.1 with Java 1.8 source/target
- **Deprecated API replacement**: Guava `Charsets` replaced with `java.nio.charset.StandardCharsets`
- **java.time API**: `java.time.LocalDate` and `DateTimeFormatter` used in place of legacy `Date`/`SimpleDateFormat` where practical
- **Parameterized logging**: SLF4J `{}` placeholders replace string concatenation in all log statements
- **Try-with-resources**: Applied for automatic resource management in `Config.loadConfig()`
- **Multi-catch blocks**: Applied in `BaseService.verifyURL()` to consolidate exception handling
- **Enhanced for-each loops**: Traditional indexed for-loops replaced with enhanced for-each where safe
- **Removed logger guards**: Unnecessary `logger.isDebugEnabled()` / `isInfoEnabled()` / `isWarnEnabled()` checks removed (SLF4J 1.6+ handles this optimization internally)

## Documentation

Documentation for the API may be found on swaggerhub
https://app.swaggerhub.com/apis/FAA/APRA
