# DevOps Lab Challenges

Welcome to the DevOps Lab challenge.

This repository contains a set of hands-on troubleshooting exercises designed to simulate realistic platform, CI/CD, Kubernetes, and observability issues. The goal is to investigate, understand, and fix problems in an environment that resembles day-to-day operational work.

## General architecture

The lab environment is built around a small cloud-native platform with the following main components:

- **Kubernetes** as the runtime environment for workloads
- **Jenkins** as the CI/CD entrypoint for building and publishing exercise images
- **Kaniko** for container image builds inside Kubernetes
- **DigitalOcean Container Registry** for storing built images
- **Grafana** for metrics visualization and dashboard creation

Each exercise represents an isolated scenario inside this platform. You may need to inspect build pipelines, Kubernetes resources, runtime behavior, logs, metrics, or dashboards depending on the challenge.

## Important rules for the challenge

- There is **no required order** to solve the exercises.
- Each exercise is **independent** from the others.
- You can choose whichever exercise you want to tackle first.
- A failure in one exercise should not be assumed to be related to another unless explicitly stated.

## Access provided at the beginning of the session

At the start of the session, you will be given the credentials and access details required to work on the platform.

This includes:

- **Access to Kubernetes**, limited to the **workload nodes/resources** used for the exercises
- **Access to Jenkins** to inspect, edit, or execute builds
- **Access to Grafana** to review metrics and create dashboards if needed

Make sure to verify your access before starting with any exercise.

## Jenkins Kaniko build reference

Use [`ci/kaniko-build.Jenkinsfile`](ci/kaniko-build.Jenkinsfile) as the pipeline definition for building exercise images inside the Kubernetes cluster.

Expected Jenkins job shape:

- Pipeline job
- Pipeline definition: `Pipeline script from SCM`
- SCM: `Git`
- Repository URL: `https://github.com/poltorres7/devops-lab-exercices.git`
- Script Path: `ci/kaniko-build.Jenkinsfile`

Required Jenkins namespace secret:

- `devopslabregistry`

The Kaniko agent mounts that secret at `/kaniko/.docker/config.json` and pushes to:

- `registry.digitalocean.com/devopslabregistry`

Useful build parameters:

- `EXERCISE_DIR`
- `GIT_REF`
- `IMAGE_TAG`
- `PUSH_LATEST`
- `UPDATE_GITOPS`
- `GIT_PUSH_CREDENTIALS_ID`

Tagging behavior:

- If `IMAGE_TAG` is empty, the pipeline uses the Git short commit hash.
- This is the recommended immutable tag format for Argo CD deployments.
- If `UPDATE_GITOPS=true`, the pipeline updates `exercises/<name>/helm/values.yaml` with that tag and pushes the change back to GitHub.
- `UPDATE_GITOPS=true` requires a Jenkins username/password credential that can push to the repository.
