#!/usr/bin/groovy

def call(Map mvnOptions) {
  script {
    configFileProvider(
      [configFile(fileId: "${mvnOptions.mvnSettingsXML}", variable: 'MAVEN_SETTINGS')])
      {
        sh "mvn ${mvnOptions.mvnDeployParams} -s $MAVEN_SETTINGS"
      }
  }
}
