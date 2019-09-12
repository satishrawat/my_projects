#!/usr/bin/groovy

def call(Map pipelineParams) {
/*----------------------------------------------------------------------------*/
/*        Job handles building/packaging/testing/deployment of an app.        */
/* Job is called from a multi-branch pipeline job.                            */
/*----------------------------------------------------------------------------*/

  pipeline {
    agent {
      kubernetes {
        //cloud 'kubernetes'
        label 'slave-pod'
        yaml """
        kind: Pod
        metadata:
          name: kaniko
        spec:
          containers:
          - name: jnlp
            image: us.gcr.io/gcrProject/jnlp-slave:3.29-1
            imagePullPolicy: Always
            command: ["/bin/sh", "-c"]
            args: ["sleep 30; echo 'Istio proxy needs to come online before jnlp'; jenkins-slave"]
          - name: maven
            image: maven:3.5.4-jdk-8
            imagePullPolicy: Always
            command:
            - cat
            tty: true
            volumeMounts:
              - name: my-pvc-nfs
                mountPath: /root/.m2
          - name: kaniko
            image: gcr.io/kaniko-project/executor:debug
            imagePullPolicy: Always
            command:
            - /busybox/cat
            tty: true
            volumeMounts:
              - name: kaniko-secret
                mountPath: /secret
            env:
              - name: GOOGLE_APPLICATION_CREDENTIALS
                value: /secret/kaniko-secret.json
          volumes:
          - name: kaniko-secret
            secret:
              secretName: kaniko-secret
          - name: my-pvc-nfs
            persistentVolumeClaim:
              claimName: apcm-pvc
              """
      }
    }

    parameters {
      booleanParam(name: 'createRelease', defaultValue: false, description: 'Create a release from the repo release branch (usually master)')
    }

    options {
      buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')
      disableConcurrentBuilds()
    }

    environment {
      currentAppVersion = ''
      currentAppGroupId = ''
      currentAppArtifactId = ''
      newAppVersion = ''
      pomInfo = ''
      build_options_file = 'scm/build_options.txt'
      dockerRegistry = ''
      iqStage = 'build'
    }

    stages {

/*----------------------------------------------------------------------------*/
/*                  Prep workspace, get repo, app/build info                  */
/*----------------------------------------------------------------------------*/

      stage('prep') {
        steps {
          echo "Deleting workspace..."
          deleteDir ()

          echo "Checking out repo..."
          checkout scm

          // echo "Getting scm tools/scripts..."
          // getScmTools ()

          dir(pipelineParams.directory) {
            script {
              echo "Getting app data..."
              pomInfo = readMavenPom file: 'pom.xml'
              currentAppVersion=pomInfo.version
              currentAppGroupId=pomInfo.groupId
              currentAppArtifactId=pomInfo.artifactId

              echo "Getting build options..."
              getBuildOptions ()
            }
          }
        }
      }

/*----------------------------------------------------------------------------*/
/* Create release if build flag is true & branch name matches releaseBranch   */
/* listed in build_options file (usually master).                             */
/*----------------------------------------------------------------------------*/

      stage('create release') {
        when {
          allOf {
            branch "${getBuildOptions.releaseBranch}"
            environment name: "createRelease", value: 'true'
          }
        }

        steps {
          dir(pipelineParams.directory) {
            script {
              container('maven') {
                // Set the iqStage to "release" for release builds
                iqStage = "release"
                currentAppVersion = createReleaseTag (appVersion: "${currentAppVersion}",
                                                      releaseBranchPrefix: "${getBuildOptions.releaseBranchPrefix}",
                                                      mvnSettingsXML: "${getBuildOptions.mvnSettingsXML}",
                                                      jenkinsSSHCredentials: "${getBuildOptions.jenkinsSSHCredentials}")
              }
            }
          }
        }
      }

/*----------------------------------------------------------------------------*/
/*                               Compile the app                              */
/*----------------------------------------------------------------------------*/

      stage('compile') {
        steps {
          dir(pipelineParams.directory) {
            script {
              container('maven') {
                mavenBuild(mvnSettingsXML: "${getBuildOptions.mvnSettingsXML}", mvnBuildParams: "${getBuildOptions.mvnBuildParams}")
                jacoco(
                        execPattern: 'target/**/*.exec',
                        classPattern: 'target/classes,**/target/classes',
                        sourcePattern: 'src/main/java,**/src/main/java',
                        exclusionPattern: 'src/test*,**/src/test*'
                )
              }
            }
          }
        }
      }

/*----------------------------------------------------------------------------*/
/* Run tests in parallel. Test execution is based on:                         */
/*    enable/disable flag set in scm/build_options file                       */
/*    branch being built - if devint, release, Pull Request, always build it  */
/*    app version - if release/snapshot version, always build it              */
/*----------------------------------------------------------------------------*/

      stage ('testing') {
        parallel {
          stage ('nexusIQ') {
            when {
              expression {
                getBuildOptions.enableNexusIQ.matches ("true") || env.BRANCH_NAME.matches ("${getBuildOptions.devIntegrationBranch}") || env.BRANCH_NAME.matches ("${getBuildOptions.releaseBranch}") || env.BRANCH_NAME.contains ("PR-") || !currentAppVersion.contains ('SNAPSHOT')
              }
            }
            steps {
              dir(pipelineParams.directory) {
                script {
                  container('maven') {
                    nexusIq(nexusIqApp: "${getBuildOptions.nexusIQ}", iqStage: "${iqStage}")
                  }
                }
              }
            }
          }

          stage ('sonar') {
            when {
              expression {
                getBuildOptions.enableSonar.matches ("true") || env.BRANCH_NAME.matches ("${getBuildOptions.devIntegrationBranch}") || env.BRANCH_NAME.matches ("${getBuildOptions.releaseBranch}") || env.BRANCH_NAME.contains ("PR-") || !currentAppVersion.contains ('SNAPSHOT')
              }
            }
            steps {
              dir(pipelineParams.directory) {
                script {
                  container ('maven') {
                    withSonarQubeEnv('sonarQube') {
                      sonar (mvnSettingsXML: "${getBuildOptions.mvnSettingsXML}", currentAppVersion: "${currentAppVersion}", releaseBranchPrefix: "${getBuildOptions.releaseBranchPrefix}", mvnBuildParams: "${getBuildOptions.mvnBuildParams}")
                    }
                    sleep(10) // https://community.sonarsource.com/t/need-a-sleep-between-withsonarqubeenv-and-waitforqualitygate-or-it-spins-in-in-progress/2265
                    timeout(time: 5, unit: 'MINUTES') {
                      waitForQualityGate abortPipeline: true
                    }
                  }
                }
              }
            }
          }
        }
      }

/*----------------------------------------------------------------------------*/
/*  Upload application artifacts to repo server only when not a Pull Request  */
/*----------------------------------------------------------------------------*/

      stage ('upload') {
        when {
          expression {
            !env.BRANCH_NAME.contains ("PR-")
          }
        }
        steps {
          dir(pipelineParams.directory) {
            script {
              container ('maven') {
                mavenDeploy(mvnSettingsXML: "${getBuildOptions.mvnSettingsXML}", mvnDeployParams: "${getBuildOptions.mvnDeployParams}")
              }
            }
          }
        }
      }

/*----------------------------------------------------------------------------*/
/*                      Kaniko  -     Build and Push Image                    */
/*----------------------------------------------------------------------------*/

      stage('image-build/push') {
        environment {
          PATH = "/busybox:/kaniko:$PATH"
        }
        steps {
          dir(pipelineParams.directory) {
            script {
              container(name: 'kaniko', shell: '/busybox/sh') {
                kaniko(gcrProjectId: "${getBuildOptions.gcrProjectId}", currentAppVersion: "${currentAppVersion}", currentAppArtifactId: "${currentAppArtifactId}")
              }
            }
          }
        }
      }
    }

/*--------------------------------------------------------------------*/
/*        Cleanup - delete workspace, email victims if job fails      */
/*--------------------------------------------------------------------*/

    post {
/*
      always {
        deleteDir ()
      }
*/
      failure {
        mail to: pipelineParams.email, subject: "Job ${env.JOB_NAME} failed", body: "Check the URL below for more info\n\n${env.BUILD_URL}"
      }
    }
  }
}
