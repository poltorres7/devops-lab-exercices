# Exercise 04: Java CI Failure

## Scenario

The application never reaches deployment because the CI pipeline fails during the build stage.

## Candidate Goals

- inspect Jenkins pipeline logs
- identify whether the failure is a test, dependency, or build-script issue
- fix the build and publish a successful image

## Intended Fault

A build-time verification step fails because one of the test expectations no longer matches the current application behavior.
