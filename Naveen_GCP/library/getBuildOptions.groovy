#!/usr/bin/groovy
/*----------------------------------------------------------------------------*/
/* Script gets/sets data from build_options file. Missing parameters are set  */
/* to 'sensible' defaults. Params are returned as a map                       */
/*----------------------------------------------------------------------------*/

def call () {
  script {

    def propertyInfo = readProperties file: build_options_file

    // Get Credentials ID of Jenkins SSH keys used to access BitBucket
    jenkinsSSHCredentials = (propertyInfo.jenkinsSSHCredentials == null  || propertyInfo.jenkinsSSHCredentials == '') ? "defaultJenkinsSSH" : propertyInfo.jenkinsSSHCredentials;

    // Get Maven, JDK versions to use
    jdkVersion  = (propertyInfo.jdkVersion == null  || propertyInfo.jdkVersion == '') ? "jdk" : propertyInfo.jdkVersion;
    mvnVersion  = (propertyInfo.mvnVersion == null  || propertyInfo.mvnVersion == '') ? "mvn" : propertyInfo.mvnVersion;

    // Get Maven parameters to use
    mvnBuildParams  = (propertyInfo.mvnBuildParams  == null || propertyInfo.mvnBuildParams == '')  ? "package"         : propertyInfo.mvnBuildParams;
    mvnDeployParams = (propertyInfo.mvnDeployParams == null || propertyInfo.mvnDeployParams == '') ? "deploy"          : propertyInfo.mvnDeployParams;
    mvnSettingsXML  = (propertyInfo.mvnSettingsXML  == null || propertyInfo.mvnSettingsXML == '')  ? "defaultMVNSettings" : propertyInfo.mvnSettingsXML;
    useLocalMvnRepo  = (propertyInfo.useLocalMvnRepo  == "true") ? "-Dmaven.repo.local=${WORKSPACE}/.m2/repository" : '';

    // Get Nexus repos to use
    yumReleaseRepo = (propertyInfo.yumReleaseRepo == null || propertyInfo.yumReleaseRepo == '') ? env.yumReleaseRepo : propertyInfo.yumReleaseRepo;
    yumSnapshotRepo = (propertyInfo.yumSnapshotRepo == null || propertyInfo.yumSnapshotRepo == '') ? env.yumSnapshotRepo : propertyInfo.yumSnapshotRepo;
    mavenReleaseRepo = (propertyInfo.mavenReleaseRepo == null || propertyInfo.mavenReleaseRepo == '') ? env.mavenReleaseRepo : propertyInfo.mavenReleaseRepo;
    mavenSnapshotRepo = (propertyInfo.mavenSnapshotRepo == null || propertyInfo.mavenSnapshotRepo == '') ? env.mavenSnapshotRepo : propertyInfo.mavenSnapshotRepo;

    // Get Nexus Registry token to use
    dockerAuthToken  = (propertyInfo.dockerAuthToken  == null || propertyInfo.dockerAuthToken == '')  ? "defaultDockerAuthToken" : propertyInfo.dockerAuthToken;

    // Get Nexus registry to use
    dockerReleaseRegistry = (propertyInfo.dockerReleaseRegistry == null || propertyInfo.dockerReleaseRegistry == '') ? env.dockerReleaseRegistry : propertyInfo.dockerReleaseRegistry;
    dockerSnapshotRegistry = (propertyInfo.dockerSnapshotRegistry == null || propertyInfo.dockerSnapshotRegistry == '') ? env.dockerSnapshotRegistry : propertyInfo.dockerSnapshotRegistry;

    // Get Projects Docker module
    dockerModule = (propertyInfo.dockerModule == null || propertyInfo.dockerModule == '') ? '' : propertyInfo.dockerModule;

    // Get GCR Project ID
    gcrProjectId = (propertyInfo.gcrProjectId == null || propertyInfo.gcrProjectId == '') ? env.gcrProjectId : propertyInfo.gcrProjectId;    

    // Get application installation service account
    appServiceAccount = (propertyInfo.appServiceAccount == null || propertyInfo.appServiceAccount == '') ? "appSvc" : propertyInfo.appServiceAccount;

    // Get Nexus IQ app name
    nexusIQ = propertyInfo.nexusIQ

    // Get devint, release branch info, release branch prefix
    releaseBranchPrefix  = (propertyInfo.releaseBranchPrefix == null  || propertyInfo.releaseBranchPrefix == '') ? "" : propertyInfo.releaseBranchPrefix;
    releaseBranch        = (propertyInfo.releaseBranch == null        || propertyInfo.releaseBranch == '') ? "master" : propertyInfo.releaseBranch;
    devIntegrationBranch = (propertyInfo.devIntegrationBranch == null || propertyInfo.devIntegrationBranch == '') ? "develop" : propertyInfo.devIntegrationBranch;

    // Get auto deployment target(s). Read value from build_options file, override with job environment variable value if set
    deployTargetsFromFile = (propertyInfo.deployTargets == null || propertyInfo.deployTargets == '') ? "" : propertyInfo.deployTargets;
    deployTargets = (env.deployTargets == null || env.deployTargets == '') ? deployTargetsFromFile : env.deployTargets

    // Get flags for running testing
    enableSonar   = (propertyInfo.enableSonar == null   || propertyInfo.enableSonar == '') ? "true"   : propertyInfo.enableSonar;
    enableNexusIQ = (propertyInfo.enableNexusIQ == null || propertyInfo.enableNexusIQ == '') ? "true" : propertyInfo.enableNexusIQ;

    // Sleep time setup for Sonar analysis
    enableSonarSleep = (propertyInfo.enableSonarSleep == null   || propertyInfo.enableSonarSleep == '') ? "15"   : propertyInfo.enableSonarSleep;

    return this
  }
}
