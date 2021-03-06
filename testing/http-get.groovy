#!/usr/bin/env groovy

/**
 ** Jenkins Module:
 * HTTP get returning RC.
 *
 ** IMPORTANT:
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 */

/**
 ** Function:
 * This function returns the http response code for a http request
 *
 ** Parameters:
 * @param String checkUrl   URL to be tested
 *
 * @return NO return value. This call will execute the tagReleaseWithLastChanges() function declared in this module.
 *
 ** Examples:
 * A) Sample usage from a Pipeline Stage(you must include the function in the same groovy script)
 *
 *  node {
 *      stage('Https curl request') {
 *           print "https response code: " + call(https://www.myapp.com)
 *
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   HTTPS_REQ = load "https_curl_request.groovy"
 *   def http_response_code = HTTPS_REQ("https://www.myapp.com")
 *   echo "http response code: " + http_response_code
 */
def call(String checkUrl){
    // Just perform a GET requst to the provided URL

    try{
        def get = new URL(checkUrl).openConnection();
        def getRC = get.getResponseCode();

        println "URL: ${checkUrl}\tRC: ${getRC}"
        return getRC

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this