# devops-lab-exercices
Public exercises for candidates, troubleshooting scenarios.

## Jenkins Kaniko Build

Use [ci/kaniko-build.Jenkinsfile](/Users/paultorresrivera/Code/github/devops-lab-exercices/ci/kaniko-build.Jenkinsfile) as the pipeline definition for building exercise images inside the Kubernetes cluster.

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
