#!/usr/bin/env groovy

/**
 ** Jenkins Modules:
 * AWS Fargate get task id
 *
 ** IMPORTANT:
 * this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *

/*
 ** Function:
 ** This function returns the fargate task ID based on the intance id passed as argument
 *
 ** Parameters:
 * @param String container_metadata_uri             ECS Fargate matadata URI.
 *
 * @return String fargateTaskId     String containing the fargate task ID
 *
 */

def getFargateTaskId(String container_metadata_uri) {

    fargateTaskId = sh(
                    returnStdout: true,
                    script: """curl -sb -H 'Accept: application/json' ${container_metadata_uri} | 
                            jq '.Labels["com.amazonaws.ecs.task-arn"]' | 
                            cut -d'/' -f2 | 
                            tr -d '"'
    """).trim()

    echo "taskId: ${fargateTaskId}"


    return fargateTaskId 

}

// Note: this line is crucial when you want to load an external groovy script
return this