
# mtd-api-nrs-proxy

The [NRS (Non-Repudiation Service)](https://confluence.tools.tax.service.gov.uk/display/NR/Non-repudiation+principles) is used to keep an audit trail of important business events/actions which could be used as a source of evidence in courts to prove a case.

The MTD API NRS Proxy was built to allow MTD APIs to send 'fire-and-forget' NRS requests. Occasionally NRS requests may be slow or time out, so
the proxy handles the retry mechanisms and error handling. 

## Requirements

- Scala 2.13.x
- Java 11
- sbt 1.7.x
- [Service Manager V2](https://github.com/hmrc/sm2)

## Development Setup

Run the microservice from the console using: `sbt run` (starts on port 7794 by default)

Start the service manager profile: `sm --start MTD_API_NRS_PROXY`

## Run Tests

Run unit tests: `sbt test`

Run integration tests: `sbt it:test`

Run all tests with coverage: `sbt clean coverage test it:test coverageReport`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
