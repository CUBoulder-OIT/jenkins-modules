#!/usr/bin/env groovy
/**
 ** Jenkins Modules:
 * Mysql restore database from uploaded file.
 *
 ** IMPORTANT:
 * This module relies on MYSQL CLI, installed in the current jenkins server and of course the dbHost to be
 * reachable to be configured to run as-is, this module does not handle that.
 *
 ** Parameters:
 * @param String mysqlUser     mysql user to be passed as parameter in the cli
 * @param String mysqlRootPass mysql user password (with the necessary permissions) to be passed as parameter in the cli
 * @param String dbHost        mysql db server host where the db from file will be restored
 * @param String dbName        mysql db name where the restore from file will be executed.
 */
def call(String mysqlUser, String mysqlRootPass, String dbName, String dbHost) {

    String userInputUploadSqlFile = ""
    try {
        stage('Would you like to upload a .sql file to run on the database?') {
            //noinspection GroovyAssignabilityCheck
            userInputUploadSqlFile = input(
                    id: 'userInputUploadSqlFile', message: 'Would you like to upload .sql file?', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices: 'Yes\nNo', description: 'Database update file', name: 'target']
            ])
            echo "SQL file is going to be uploaded: ${userInputUploadSqlFile}"
        }

        if (userInputUploadSqlFile == 'Yes') {
            stage('file input') {
                // Get file using input step, will put it in build directory
                //noinspection GroovyAssignabilityCheck
                def inputFile = input message: 'Upload file', parameters: [file(name: 'migration.sql')]
                // Read contents and write to workspace
                writeFile(file: 'migration.sql', text: inputFile.readToString())
                // Stash it for use in a different part of the pipeline
                stash name: 'data', includes: 'migration.sql'

                String mysqlFileCode = readFile 'migration.sql'
                echo "Content of input file: ${mysqlFileCode}"

                File mysqlFile = new File("${env.WORKSPACE}/migration.sql")

                if (!mysqlFile.exists()) {
                    echo ("File does not exist")
                } else {
                    echo ("File does EXISTS")
                }
            }

            stage("run SQL file on MYSQL dbHost: ${dbHost} and to dbName: ${dbName}") {
                sh "#!/bin/bash \n" +
                        "cat migration.sql | mysql -h ${dbHost} -u ${mysqlUser} --password=${mysqlRootPass} ${dbName}"
            }
        }

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this

