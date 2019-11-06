#!/usr/bin/groovy

def call(Map iqOptions) {
  script {
    nexusPolicyEvaluation failBuildOnNetworkError: false, iqApplication: selectedApplication("${iqOptions.nexusIqApp}"), iqStage: "${iqOptions.iqStage}", jobCredentialsId: ''
  }
}
