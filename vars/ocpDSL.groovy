#!/groovy

/**
 * Methods based on the OpenShift Jenkins Pipeline (DSL) Plugin.
 * https://github.com/jenkinsci/openshift-client-plugin
 */


/**
 * Gets the latest generation of the tag of the imageStream.
 *
 * @return the hash of the latest generation
 */
def call(Map config) {
    echo "getImageStreamHash " + config.imageStreamName + " " + config.tagName
    echo "openshift " + openshift
    echo "openshift sel " + openshift.selector("is/${config.imageStreamName}")
    echo "past sel "
    echo "openshift sel obj" + openshift.selector("is/${config.imageStreamName}").object()
    echo "past sel obj"
    // TDOO check for openshift step

    def isSelector = openshift.selector("is/${config.imageStreamName}").object()
    echo "${config.imageStreamName} IS from -> " + isSelector.spec.tags['from'].name

    // sha of latest tag from is of DockerImage
    // oc get is memcached -o jsonpath='{.status.tags[?(@.tag=="latest")].items[*].image}{"\n"}'
    def tags = isSelector.status.tags
    def latestTag = tags.find { it.tag == config.tagName }
    echo tagName + " tag: " + latestTag
    // TODO check for null

    def latestTagHash = latestTag.items[0].image
    echo tagName + "latest tag image: " + latestTagHash
    // TODO takte latest generation of timestamp, instad of index 0

    return latestTagHash
}

return this;
