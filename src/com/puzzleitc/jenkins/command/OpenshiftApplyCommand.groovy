package com.puzzleitc.jenkins.command

import com.puzzleitc.jenkins.command.context.PipelineContext

import static com.puzzleitc.jenkins.command.Constants.DEFAULT_OC_TOOL_NAME

class OpenshiftApplyCommand {

    private static final VALID_ROLLOUT_KINDS =
            ['deploymentconfig', 'dc', 'deployment', 'deploy', 'daemonset', 'ds', 'statefulset', 'sts']

    private final PipelineContext ctx

    OpenshiftApplyCommand(PipelineContext ctx) {
        this.ctx = ctx
    }

    void execute() {
        ctx.info('-- openshiftApply --')
        def configuration = ctx.stepParams.getRequired('configuration') as String
        def project = ctx.stepParams.getRequired('project')
        def cluster = ctx.stepParams.getOptional('cluster')
        def appLabel = ctx.stepParams.getRequired('appLabel') as String
        def waitForRollout = ctx.stepParams.getOptional('waitForRollout', true) as boolean
        def rolloutKind = ctx.stepParams.getOptional('rolloutKind', 'dc') as String
        def rolloutSelector = ctx.stepParams.getOptional('rolloutSelector', [:]) as Map
        def credentialsId = ctx.stepParams.getOptional('credentialsId') as String
        def saToken = ctx.lookupServiceAccountToken(credentialsId, project)
        ctx.withEnv(["PATH+OC=${ctx.executable('oc', DEFAULT_OC_TOOL_NAME)}"]) {
            ctx.openshift.withCluster(cluster) {
                ctx.openshift.withProject(project) {
                    ctx.openshift.withCredentials(saToken) {
                        ctx.echo("openshift whoami: ${ctx.openshift.raw('whoami').out.trim()}")
                        ctx.echo("openshift cluster: ${ctx.openshift.cluster()}")
                        ctx.echo("openshift project: ${ctx.openshift.project()}")
                        ocConvert(configuration)
                        ocApply(configuration, appLabel)
                        if (waitForRollout) {
                            ocWaitForRollout(appLabel, rolloutKind, rolloutSelector)
                        }
                    }
                }
            }
        }
    }

    private void ocConvert(String configuration) {
        ctx.doWithTemporaryFile(configuration, '.yaml', 'UTF-8') {
            String absolutePath ->
                def result = ctx.openshift.raw('convert', '-f', absolutePath)
                ctx.echo("oc convert action: ${result.actions[0].cmd}")
                ctx.echo("oc convert status: ${result.status}")
        }
    }

    private void ocApply(String configuration, String appLabel) {
        def result = ctx.openshift.apply(configuration, '-l', "app=${appLabel}", '--prune')
        ctx.echo("oc apply action: ${result.actions[0].cmd}")
        ctx.echo("oc apply status: ${result.status}")
        ctx.echo("oc apply output:\n${result.out}")
    }

    private void ocWaitForRollout(String appLabel, String rolloutKind, Map rolloutSelector) {
        validateRolloutKind(rolloutKind)
        if (rolloutSelector.isEmpty()) {
            ctx.echo("waiting for '${rolloutKind}' with selector '${appLabel}' to be rolled out")
            ctx.openshift.selector(rolloutKind, appLabel).rollout().status()
        } else {
            ctx.echo("waiting for '${rolloutKind}' with selector ${rolloutSelector} to be rolled out")
            ctx.openshift.selector(rolloutKind, rolloutSelector).rollout().status()
        }
    }

    private void validateRolloutKind(String rolloutKind) {
        if (!VALID_ROLLOUT_KINDS.contains(rolloutKind?.toLowerCase())) {
            ctx.fail("Unsupported rollout kind: ${rolloutKind}")
        }
    }

}
