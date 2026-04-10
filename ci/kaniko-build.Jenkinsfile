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
      defaultValue: 'latest',
      description: 'Container image tag to publish.'
    )
    booleanParam(
      name: 'PUSH_LATEST',
      defaultValue: true,
      description: 'Also publish the latest tag when IMAGE_TAG is different.'
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
        }

        container('git') {
          sh '''
            set -eu
            rm -rf source
            git clone --depth 1 --branch "${GIT_REF}" "${REPO_URL}" source
          '''
        }

        container('kaniko') {
          sh '''
            set -eu
            CONTEXT_DIR="${WORKSPACE}/source/exercises/${EXERCISE_DIR}/app"
            DESTINATIONS="--destination=${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"

            if [ "${PUSH_LATEST}" = "true" ] && [ "${IMAGE_TAG}" != "latest" ]; then
              DESTINATIONS="${DESTINATIONS} --destination=${REGISTRY}/${IMAGE_NAME}:latest"
            fi

            /kaniko/executor \
              --context="${CONTEXT_DIR}" \
              --dockerfile="${CONTEXT_DIR}/Dockerfile" \
              ${DESTINATIONS} \
              --cache=true \
              --cache-copy-layers \
              --snapshot-mode=redo \
              --use-new-run
          '''
        }
      }
    }
  }
}
