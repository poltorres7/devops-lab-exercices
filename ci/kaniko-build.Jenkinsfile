pipeline {
  agent none

  parameters {
    choice(
      name: 'EXERCISE_DIR',
      choices: [
        '01-network-db-connectivity',
        '02-java-restart-loop',
        '03-java-resource-starvation',
        '04-java-ci-failure',
        '05-java-metrics-gap'
      ],
      description: 'Exercise directory to build from the repo.'
    )
    string(
      name: 'GIT_REF',
      defaultValue: 'main',
      description: 'Git branch, tag, or commit to build.'
    )
    string(
      name: 'IMAGE_TAG',
      defaultValue: '',
      description: 'Container image tag to publish. Leave empty to use the Git short commit hash.'
    )
    booleanParam(
      name: 'PUSH_LATEST',
      defaultValue: false,
      description: 'Also publish the latest tag when IMAGE_TAG is different.'
    )
    booleanParam(
      name: 'UPDATE_GITOPS',
      defaultValue: false,
      description: 'Update the exercise Helm values with the built image tag and push the change back to Git.'
    )
    string(
      name: 'GIT_PUSH_CREDENTIALS_ID',
      defaultValue: '',
      description: 'Optional Jenkins username/password credential id for pushing GitOps tag updates back to GitHub.'
    )
  }

  environment {
    REPO_URL = 'https://github.com/poltorres7/devops-lab-exercices.git'
    REGISTRY = 'registry.digitalocean.com/devopslabregistry'
  }

  stages {
    stage('Build And Push') {
      agent {
        kubernetes {
          defaultContainer 'kaniko'
          yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    app: kaniko-builder
spec:
  serviceAccountName: jenkins
  nodeSelector:
    workload: lab
  tolerations:
    - key: dedicated
      operator: Equal
      value: lab
      effect: NoSchedule
  containers:
    - name: git
      image: alpine/git:2.47.2
      command:
        - /bin/sh
      args:
        - -c
        - cat
      tty: true
    - name: kaniko
      image: gcr.io/kaniko-project/executor:v1.23.2-debug
      command:
        - /busybox/cat
      tty: true
      volumeMounts:
        - name: registry-config
          mountPath: /kaniko/.docker
  volumes:
    - name: registry-config
      secret:
        secretName: devopslabregistry
        items:
          - key: .dockerconfigjson
            path: config.json
"""
        }
      }

      steps {
        script {
          def images = [
            '01-network-db-connectivity': 'network-db-connectivity',
            '02-java-restart-loop': 'java-restart-loop',
            '03-java-resource-starvation': 'java-resource-starvation',
            '04-java-ci-failure': 'java-ci-failure',
            '05-java-metrics-gap': 'java-metrics-gap',
          ]

          env.IMAGE_NAME = images[params.EXERCISE_DIR]
          if (!env.IMAGE_NAME) {
            error("Unsupported exercise directory: ${params.EXERCISE_DIR}")
          }

          env.GITOPS_VALUES_FILE = "exercises/${params.EXERCISE_DIR}/helm/values.yaml"
        }

        container('git') {
          sh '''
            set -eu
            rm -rf source
            git clone "${REPO_URL}" source
            cd source
            git checkout "${GIT_REF}"
          '''
          script {
            env.GIT_SHORT_SHA = sh(
              script: 'cd source && git rev-parse --short HEAD',
              returnStdout: true
            ).trim()
            env.EFFECTIVE_IMAGE_TAG = params.IMAGE_TAG?.trim() ? params.IMAGE_TAG.trim() : env.GIT_SHORT_SHA
          }
        }

        container('kaniko') {
          sh '''
            set -eu
            CONTEXT_DIR="${WORKSPACE}/source/exercises/${EXERCISE_DIR}/app"
            DESTINATIONS="--destination=${REGISTRY}/${IMAGE_NAME}:${EFFECTIVE_IMAGE_TAG}"

            if [ "${PUSH_LATEST}" = "true" ] && [ "${EFFECTIVE_IMAGE_TAG}" != "latest" ]; then
              DESTINATIONS="${DESTINATIONS} --destination=${REGISTRY}/${IMAGE_NAME}:latest"
            fi

            /kaniko/executor \
              --context="${CONTEXT_DIR}" \
              --dockerfile="${CONTEXT_DIR}/Dockerfile" \
              ${DESTINATIONS} \
              --snapshot-mode=redo \
              --use-new-run
          '''
        }

        script {
          currentBuild.displayName = "#${env.BUILD_NUMBER} ${params.EXERCISE_DIR} ${env.EFFECTIVE_IMAGE_TAG}"
          currentBuild.description = "${env.IMAGE_NAME}:${env.EFFECTIVE_IMAGE_TAG}"
        }

        container('git') {
          script {
            if (params.UPDATE_GITOPS) {
              if (!params.GIT_PUSH_CREDENTIALS_ID?.trim()) {
                error('UPDATE_GITOPS=true requires GIT_PUSH_CREDENTIALS_ID.')
              }

              withCredentials([usernamePassword(
                credentialsId: params.GIT_PUSH_CREDENTIALS_ID.trim(),
                usernameVariable: 'GIT_USERNAME',
                passwordVariable: 'GIT_PASSWORD'
              )]) {
                sh '''
                  set -eu
                  cd source
                  git config user.name "Jenkins"
                  git config user.email "jenkins@lab.local"
                  sed -i.bak -E "s#^(  tag: ).*#\\1${EFFECTIVE_IMAGE_TAG}#" "${GITOPS_VALUES_FILE}"
                  rm -f "${GITOPS_VALUES_FILE}.bak"

                  if git diff --quiet -- "${GITOPS_VALUES_FILE}"; then
                    exit 0
                  fi

                  git add "${GITOPS_VALUES_FILE}"
                  git commit -m "Update ${EXERCISE_DIR} image tag to ${EFFECTIVE_IMAGE_TAG}"
                  git remote set-url origin "https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/poltorres7/devops-lab-exercices.git"
                  git push origin HEAD:main
                '''
              }
            }
          }
        }
      }
    }
  }
}
