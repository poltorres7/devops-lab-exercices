# Exercise 01: Networking DB Connectivity

## Scenario

A Java service is expected to become healthy only after its database dependency is reachable. The current deployment never stabilizes, and the candidate must determine whether the issue is related to service discovery, endpoint selection, port reachability, or connection settings.

## Candidate Goals

- inspect pod restart behavior and logs
- use `nc`, `dig`, `nslookup`, and similar tooling
- validate both hostname resolution and port reachability
- update Helm values and redeploy

## Intended Fault

The chart ships with database connection settings that do not match the real target endpoint, so the application fails during startup dependency validation.

## Remediation

Update `helm/values.yaml` with the correct database connection settings and redeploy.
