company#!/usr/bin/groovy

def call(Map releaseOptions) {
/*----------------------------------------------------------------------------*/
/* Create a release tag. Commands assume SSH connectivity to enable upload    */
/* back to Bitbucket; connectivity can be set through BB Branch Source plugin */
/*----------------------------------------------------------------------------*/

  script {
    // Bitbucket Branch Source plugin only pulls refspecs for single branch.
    // Get all release info to determine next version to use.
    sshagent(["${releaseOptions.jenkinsSSHCredentials}"]) {
      sh "git config core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no'"
      sh "git config --global user.email 'Jenkins@company.com'"
      sh "git config --global user.name 'Jenkins'"

      sh "git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*"
      sh "git fetch --tags -v"

      // Get major.minor number in pom.xml

      def currentVersion=releaseOptions.appVersion.replace ('-SNAPSHOT', '')
      echo "Found version ${currentVersion} in pom.xml"

      // Get latest version with 'git describe' from release Branch
      // Tags on this branch should use release version prefix & 3 digit numbering convention
      // If no tag found ('|| true' construct in shell cmd), assume this is a first release version.

      def releaseTagPattern = "${releaseOptions.releaseBranchPrefix}${currentVersion}.[0-9]*"
      latestReleaseTag = sh (script: "git describe --match \"${releaseTagPattern}\" --abbrev=0 || true", returnStdout: true)

      if (latestReleaseTag.length().compareTo(0) == 0) {
      // No matching release pattern found
        echo "No release tags matching ${releaseTagPattern} found"
        newMinorVersion = '0'.toInteger()
      }
      else
      {
        // Found a release string, get next release version from 3rd digit
        echo "Found latest release tag ${latestReleaseTag}"
        tmpArr = latestReleaseTag.split ("\\.")
        newMinorVersion = tmpArr.last ().toInteger()+1
      }

      newAppVersion = "${currentVersion}.${newMinorVersion}"
      echo "Creating Release tag ${releaseOptions.releaseBranchPrefix}${newAppVersion}"

      // update pom files so apps will be built with new version, push only tag to BitBucket
      configFileProvider(
        [configFile(fileId: "${releaseOptions.mvnSettingsXML}", variable: 'MAVEN_SETTINGS')])
        {
          sh "mvn -s $MAVEN_SETTINGS versions:set -DnewVersion=${newAppVersion} -DgenerateBackupPoms=false"
        }

      // Only push tag, not pom file version changes
        sh "git tag -a ${releaseOptions.releaseBranchPrefix}${newAppVersion} -m\"Auto: created release tag ${releaseOptions.releaseBranchPrefix}${newAppVersion}\""
        sh "git push origin ${releaseOptions.releaseBranchPrefix}${newAppVersion}"
    }
  }
  return newAppVersion
}
