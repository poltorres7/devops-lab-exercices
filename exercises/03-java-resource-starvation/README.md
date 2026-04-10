# Exercise 03: Java Resource Starvation

## Scenario

The application code is functional, but the deployed workload does not remain stable under its current runtime resource profile.

## Candidate Goals

- inspect pod events
- identify OOMKilled or resource pressure
- update Helm values
- redeploy through GitOps

## Intended Fault

The application performs significant allocation during startup while the chart defines memory settings that are too restrictive for the observed runtime behavior.
