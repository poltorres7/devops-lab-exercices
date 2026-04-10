# Exercise 02: Java Restart Loop

## Scenario

The app crashes immediately after startup because one of its startup-time configuration values is invalid.

## Candidate Goals

- inspect pod restart counts
- read stack traces carefully
- identify the configuration or code path failing during bootstrap
- rebuild or reconfigure and redeploy the fixed image

## Intended Fault

The application fails during startup initialization due to an invalid value consumed by a startup runner.
