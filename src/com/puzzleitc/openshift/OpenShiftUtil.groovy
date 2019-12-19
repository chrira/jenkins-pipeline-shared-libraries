package com.puzzleitc.openshift

/**
 * Convenient methods for OpenShift management.
 */
class OpenShiftUtil {

    /**
     * OpenShift build
     * 
     * Given the dir parameter will start a binary build with the content of that directory.
     * Leaving dir parameter empty will start a noraml build.
     *
     * method parameters:
     * ocpUrl -> url of the OCP server
     * ocpProject -> project-name/namespace of the build project
     * buildConfig -> name of the buildConfig to be used for the build
     * dir -> local directory with the files used for the build
     * credentialsId -> credentials for the OpenShift login
     */
    @com.cloudbees.groovy.cps.NonCPS
    static void build(String ocpUrl, String ocpProject, String buildConfig, String dir, String credentialsId) {

        if (!dir?.trim()) {
            echo "-- start build: $buildConfig --"
            echo "OCP server URL: $ocpUrl"
            echo "OCP project: $ocpProject"
        } else {
            echo "-- start binary build: $buildConfig --"
            echo "OCP server URL: $ocpUrl"
            echo "OCP project: $ocpProject"
            echo "context directory: $dir"
        }

        withCredentials([[$class        : 'UsernamePasswordMultiBinding',
                        credentialsId   : "${credentialsId}",
                        passwordVariable: 'openshift_password',
                        usernameVariable: 'openshift_username']]) {
            withEnv(["KUBECONFIG=${pwd()}/.kube", "PATH+OC_HOME=${tool 'oc'}/bin", "ocpUrl=${ocpUrl}"]) {
                sh "oc login $ocpUrl --insecure-skip-tls-verify=true --token=$openshift_password"
                sh "oc project $ocpProject"
                sh "oc project"
                sh "oc whoami"

                if (!dir?.trim()) {
                    // start build
                    sh "oc start-build $buildConfig --follow"
                } else {
                    // start binary build with local input
                    sh "oc start-build $buildConfig --follow --from-dir=$dir"
                }
            }
        }
    }

//
// OpenShift resource update with parameter from file
//
// Applies the content of the file to the ocpProject.
// Expects an envFile with key-value pairs holing the parameter for the template.
//
// method parameters:
// ocpUrl -> url of the OCP server
// ocpProject -> project-name/namespace of the build project
// file -> resource definition
// credentialsId -> credentials for the OpenShift login
// envFile -> environment file
// namespace -> true adds NAMESPACE_NAME param
//
    @com.cloudbees.groovy.cps.NonCPS
    static void updateResourcesWithEnvFile(String ocpUrl, String ocpProject, String file, String credentialsId, String envFile, boolean namespace) {

        echo "-- start resource update with environment form file --"
        echo "OCP server URL: $ocpUrl"
        echo "OCP project: $ocpProject"
        echo "resource file: $file"
        echo "environment file: $envFile"

        withCredentials([[$class        : 'UsernamePasswordMultiBinding',
                        credentialsId   : "${credentialsId}",
                        passwordVariable: 'openshift_password',
                        usernameVariable: 'openshift_username']]) {
            withEnv(["KUBECONFIG=${pwd()}/.kube", "PATH+OC_HOME=${tool 'oc'}/bin", "ocpUrl=${ocpUrl}"]) {
                sh "oc login $ocpUrl --insecure-skip-tls-verify=true --token=$openshift_password"
                sh "oc project $ocpProject"
                sh "oc project"
                sh "oc whoami"

                // apply template
                if (namespace) {
                    sh "oc process -f $file -p NAMESPACE_NAME=\$(oc project -q) --param-file $envFile | oc apply -f -"
                } else {
                    sh "oc process -f $file --param-file $envFile | oc apply -f -"
                }
            }
        }
    }

//
// OpenShift resource update
//
// Applies the content of the file to the ocpProject.
//
// method parameters:
// ocpUrl -> url of the OCP server
// ocpProject -> project-name/namespace of the build project
// file -> resource definition
// credentialsId -> credentials for the OpenShift login
//
    @com.cloudbees.groovy.cps.NonCPS
    static void updateResources(String ocpUrl, String ocpProject, String file, String credentialsId) {

        echo "-- start resource update --"
        echo "OCP server URL: $ocpUrl"
        echo "OCP project: $ocpProject"
        echo "resource file: $file"

        withCredentials([[$class        : 'UsernamePasswordMultiBinding',
                        credentialsId   : "${credentialsId}",
                        passwordVariable: 'openshift_password',
                        usernameVariable: 'openshift_username']]) {
            withEnv(["KUBECONFIG=${pwd()}/.kube", "PATH+OC_HOME=${tool 'oc'}/bin", "ocpUrl=${ocpUrl}"]) {
                sh "oc login $ocpUrl --insecure-skip-tls-verify=true --token=$openshift_password"
                sh "oc project $ocpProject"
                sh "oc project"
                sh "oc whoami"

                // apply template
                sh "oc process -f $file | oc apply -f -"
            }
        }
    }

//
// OpenShift image promotion
//
// Promotes a Docker Image from one project to another by tagging it correctly.
//
// method parameters:
// sourceProject -> project holding the image to be promoted
// sourceImageStream -> source image stream name (inside source project)
// sourceTag -> source image stream tag (of source image stream)
// targetProject -> project to receive the image (next stage)
// targetImageStream -> target image stream name (inside target project)
// targetTag -> target image stream tag (of target image stream)
// ocpUrl -> url of the OCP server
// credentialsId -> credentials for the OpenShift login
//
    @com.cloudbees.groovy.cps.NonCPS
    static void promote(String sourceProject, String sourceImageStream, String sourceTag,
                    String targetProject, String targetImageStream, String targetTag,
                    String ocpUrl, String credentialsId) {

        echo "-- promote image from $sourceProject to $targetProject --"
        echo "OCP server URL: $ocpUrl"
        echo "OCP source image stream [project/ImageStream:Tag]: $sourceProject/$sourceImageStream:$sourceTag"
        echo "OCP target image stream [project/ImageStream:Tag]: $targetProject/$targetImageStream:$targetTag"

        withCredentials([[$class        : 'UsernamePasswordMultiBinding',
                        credentialsId   : "${credentialsId}",
                        passwordVariable: 'openshift_password',
                        usernameVariable: 'openshift_username']]) {
            withEnv(["KUBECONFIG=${pwd()}/.kube", "PATH+OC_HOME=${tool 'oc'}/bin", "ocpUrl=${ocpUrl}"]) {
                sh "oc login $ocpUrl --insecure-skip-tls-verify=true --token=$openshift_password"
                sh "oc project $targetProject"
                sh "oc project"
                sh "oc whoami"

                // tag the target image stream
                sh "oc tag $sourceProject/$sourceImageStream:$sourceTag $targetProject/$targetImageStream:$targetTag"
            }
        }
    }
}
