#!/usr/bin/groovy

def call(Map kanikoOptions) {
  script {
    sh "/kaniko/executor --build-arg JAR_FILE=${kanikoOptions.currentAppArtifactId}-${kanikoOptions.currentAppVersion}.jar -f `pwd`/Dockerfile -c `pwd` --skip-tls-verify --destination=us.gcr.io/${kanikoOptions.gcrProjectId}/${kanikoOptions.currentAppArtifactId}:${kanikoOptions.currentAppVersion}"
  }
}
