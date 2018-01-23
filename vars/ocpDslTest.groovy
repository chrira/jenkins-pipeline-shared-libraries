#!/groovy

/**
 * Methods based on the OpenShift Jenkins Pipeline (DSL) Plugin.
 * https://github.com/jenkinsci/openshift-client-plugin
 */

def call(Map config) {

    echo "ocpDslTest - name: ${config.name}"

    sh 'env'

    sh 'oc version'

  //  sh 'oc project'
  /* geht nicht, siehe error
+ oc project
error: Missing or incomplete configuration info.  Please login or point to an existing, complete config file:

  1. Via the command-line flag --config
  2. Via the KUBECONFIG environment variable
  3. In your home directory as ~/.kube/config

To view or setup config directly use the 'config' command.
See 'oc project -h' for help and examples.
  */

    echo "===== script ====="
    def script = config.script
    echo "script " + script
    echo "script.openshift " + script.openshift
    def svRes = script.openshift.raw('version')
    echo "soc version: ${svRes.out}"

    script.openshift.withProject(config.project) {
        echo "Running in project: ${script.openshift.project()}"

        def spRes = script.openshift.raw('project')
        echo "soc project: ${spRes.out}"

    }


    /*
    echo "===== pure ====="
    echo "openshift " + openshift

    def vRes = openshift.raw('version')
    echo "oc version: ${vRes.out}"

    def pRes = openshift.raw('project')
    echo "oc project: ${pRes.out}"
    */

}

return this;
