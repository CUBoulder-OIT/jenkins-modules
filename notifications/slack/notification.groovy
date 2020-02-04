#!/usr/bin/env groovy

/**
 * Jenkins Module:
 * Slack notification utilities.
 *
 ** IMPORTANT:
 * This functions depends on Slack Notification Plugin, also consider this plugins deps. (https://plugins.jenkins.io/slack).
 * Ref link: https://jenkins.io/doc/pipeline/steps/slack/
 *
 * This function depends on Last Changes Plugins, also consider this plugins deps. (https://plugins.jenkins.io/last-changes).
 * Ref link: https://jenkins.io/doc/pipeline/steps/last-changes/
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 ** Function:
 * Send message with given color.
 *
 ** Parameters:
 *  @param String message      Text message
 *  @param String colorCode    Hexadecimal color code
 *
 * @return NO return value. This call will execute the slackSend() funciton included in this module.
 */
def send(String message, String colorCode) {
    // BooleanExpression ?If_True_Use_This_Expression :If_False_Use_This_Expression
    colorCode = colorCode ?: '#000000'
    message = message ?: ''

    if (message != '') {
        slackSend(color: colorCode, message: message)
    }
}

/**
 ** Function:
 * Send build status message using predefined template.
 *
 ** Parameters:
 *  @param String status              Possible jobs status: 'STARTED', 'SUCCESS' or 'FAILURE'.
 *  @param String secondaryMessage    Optional secondary message to be added after the standard auto-completed 1ry message.
 *
 * @return NO return value. This call will execute the send() code module function.
 *
 ** Example:
 * slackHelper = load "${JENKINSFILES_MODS}/slack/notification.groovy"
 * try {
 *      stage ('Notify Started') {
 *          slackHelper.sendBuildStatus('STARTED')
 *      }
 *
 *      // Your job stages here
 *      stage ('Notify Success') {
 *          slackHelper.sendBuildStatus('SUCCESS')
 *      }
 *
 *   } catch (e) {
 *      // If there was an exception thrown, the build failed
 *      slackHelper.sendBuildStatus('FAILURE')
 *      currentBuild.result = "FAILURE"
 *      throw e as Throwable
 *  }
 */
def sendBuildStatus(String status, String secondaryMessage = "") {
    def color = getColorByBuildStatus(status)
    def message = "${status}: Job ${env.JOB_NAME} <${env.BUILD_URL}|#${env.BUILD_NUMBER}>. "
    message += secondaryMessage

    send(message, color)
}

def sendOitCiBuildStatus(String status, String taskId, String moduleName) {
    def color = getColorByBuildStatus(status)
    def message = "${status}: Log for ${env.JOB_NAME}: "
    message += "https://oit-cloud-jenkins-state.s3-us-west-2.amazonaws.com/${taskId}/${moduleName}-console-output.txt"

    send(message, color)
}
/**
 ** Function:
 * Send build status message using predefined template adding lastChanges plugin output.
 *
 ** Parameters:
 *  @param String status              Possible jobs status: 'STARTED', 'SUCCESS' or 'FAILURE'.
 *  @param String secondaryMessage    Optional secondary message to be added after the standard auto-completed 1ry message.
 *
 * @return NO return value. This call will execute the send() code module function adding lastChanges to the passed message.
 *
 ** Example:
 * slackHelper = load "${JENKINSFILES_MODS}/slack/notification.groovy"
 * try {
 *      stage ('Notify Started') {
 *          slackHelper.sendBuildStatuslastChanges('STARTED')
 *      }
 *
 *      // Your job stages here
 *      stage ('Notify Success') {
 *          slackHelper.sendBuildStatuslastChanges('SUCCESS')
 *      }
 *
 *   } catch (e) {
 *      // If there was an exception thrown, the build failed
 *      slackHelper.sendBuildStatuslastChanges('FAILURE')
 *      currentBuild.result = "FAILURE"
 *      throw e as Throwable
 *  }
 */
def sendBuildStatuslastChanges(String status, String secondaryMessage = "") {
    def color = getColorByBuildStatus(status)
    def message = "${status}: Job ${env.JOB_NAME} <${env.BUILD_URL}|#${env.BUILD_NUMBER}>. "
    def lastChanges = "\n Last Changes: (${env.BUILD_URL}last-changes/)" as Object

    message += lastChanges
    message += secondaryMessage

    send(message, color)
}

/*
 * Send a release notification using a predefined format and optionally a custom
 * Slack channel.
 *
 *  @param appName          The name of the application
 *  @param envName          The name of the environment
 *  @param commitsMessage   A message with the commits delta for this release
 *  @param compareUrl       An URL that can be used to compare the commits delta
 *  @param channel          An Slack channel for posting this notification
 */
def notifyRelease(appName, envName, commitsMessage, compareUrl, channel) {
    def color = getColorByBuildStatus('SUCCESS')
    String msg = "${commitsMessage} \n"

    def attachments = [[
                               title: "<${compareUrl}|Commits included this release>",
                               pretext: "*NEW RELEASE* \nA new version of *${appName}* application is available on *${envName}* environment.",
                               text: msg,
                               color: color,
                       ]]

    slackSend(channel: channel, attachments: attachments)
}

/**
 ** Function:
 * Set color code identifier for different jenkins job stage status: STARTED, SUCCESS and FAILURE.
 *
 ** Parameters:
 *  @param String status    Possible jobs status: 'STARTED', 'SUCCESS' or 'FAILURE'.
 *
 * @return String colorCode. This call will execute the send() code module function adding lastChanges to the passed message.
 */
def getColorByBuildStatus(String status) {
    String redColor = '#DC3545'
    String greenColor = '#A9D071'
    String blueColor = '#3C8FD3'

    String colorCode = '#000000'
    if (status == 'STARTED') {
        colorCode = blueColor
    } else if (status == 'SUCCESS') {
        colorCode = greenColor
    } else if (status == 'FAILURE') {
        colorCode = redColor
    }

    return colorCode
}

// Note: this line is crucial when you want to load an external groovy script
return this