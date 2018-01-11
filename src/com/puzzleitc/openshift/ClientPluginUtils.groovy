package com.puzzleitc.openshift

/**
 * Docker Hub API utility class.
 */
class ClientPluginUtils {

    private final script

    /**
     * This constructor sets the Jenkins 'script' class as the local script
     * variable in order to resolve execution steps (echo, etc).
     * @param script the script object
     */
    ClientPluginUtils(script) {
        this.script = script
    }

    /**
     * Does a login into the Docker Hub API and returns the resulting token.
     * Returning null means no successful login.
     *
     * @return the Docker Hub API login token
     */
    @com.cloudbees.groovy.cps.NonCPS
    String createLoginToken(String openShiftCluster, String openShiftProject, String openShiftDeployTokenName, String imageStreamName, String tagName) {
        script.sh "oc version"
       script.openshift.withCluster(openShiftCluster, openShiftDeployTokenName) {
           script.openshift.withProject(openShiftProject) {
               script.println "Running in project: ${script.openshift.project()}"

               script.println "is sel " + script.openshift.selector("is/${imageStreamName}")
               def isSelector = script.openshift.selector("is/${imageStreamName}").object()
               script.println "${imageStreamName} IS from -> " + isSelector.spec.tags['from'].name

           }
       }
    }
}
