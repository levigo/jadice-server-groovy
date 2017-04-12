/*
 * jenkins 2 Build Pipeline for the jadice server groovy scripts
 *
 * For more documentation / syntax see
 *  - https://jenkins.io/blog/2016/12/19/declarative-pipeline-beta/
 *  - https://jenkins.io/doc/book/pipeline/syntax/
 *
 * This Build Pipeline has the following requirements:
 *  - jenkins with a docker extension
 *  - a maven settings file stored in the jenkins credentials provider under key "maven-settings"
 *
 */
pipeline {
    // Run build in a docker container
	agent {
		docker 'maven:3.3.9-jdk-7'
	}
	options {
		timeout (time: 10, unit: 'MINUTES')
	}
	stages {
		stage('Compile') {
			environment {
				MVN_SETTINGS = credentials('maven-settings')
			}
			steps {
				sh "mvn -B -s $MVN_SETTINGS -gs $MVN_SETTINGS install -Dmaven.test.failure.ignore=true"
			}
		}
	}
	post {
		always {
			deleteDir()
		}
	}
}