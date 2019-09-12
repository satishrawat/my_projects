#!/usr/bin/groovy

def call(Map sonarOptions) {
  script {

    //To show releases in SonarQube, check app version - if release use it, else use git branch name
    bName = (sonarOptions.currentAppVersion.contains ('SNAPSHOT')) ? "${env.BRANCH_NAME}" : "${sonarOptions.releaseBranchPrefix}${sonarOptions.currentAppVersion}"

    configFileProvider([configFile(fileId: "${sonarOptions.mvnSettingsXML}", variable: 'MAVEN_SETTINGS')]) {
      sh "mvn sonar:sonar -Dsonar.branch.name=${bName} ${sonarOptions.mvnBuildParams} -s $MAVEN_SETTINGS "
    }
  }
}
