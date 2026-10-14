import org.course.ImageTag

/**
 * A complete, reusable CI/CD pipeline for the course's Python services.
 *
 * Usage in a project's Jenkinsfile:
 *
 *   @Library('course-shared-lib') _
 *   standardPythonPipeline(
 *       appName:       'visitor-counter',
 *       dockerHubUser: 'myuser',
 *       deployPort:    '5000'
 *   )
 */
def call(Map config) {
    String appName  = config.appName ?: error('standardPythonPipeline: appName is required')
    String registry = config.get('registry', 'dockerhub')
    String owner    = registry == 'ecr' ? config.awsAccountId : config.dockerHubUser
    String region   = config.get('awsRegion', 'us-east-1')

    pipeline {
        agent { label config.get('agentLabel', 'docker') }
        options {
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: '20'))
            timeout(time: 30, unit: 'MINUTES')
        }
        stages {
            stage('Prepare') {
                steps {
                    script {
                        def t = new ImageTag(this)
                        env.IMAGE_TAG  = t.tag()
                        env.IMAGE_REPO = t.repo(registry, owner, appName, region)
                    }
                    echo "Image: ${env.IMAGE_REPO}:${env.IMAGE_TAG}"
                }
            }
            stage('Lint & Unit Test') {
                steps { pythonBuild() }
            }
            stage('Trivy FS Scan') {
                steps { trivyScan(type: 'fs') }
            }
            stage('Docker Build') {
                steps { dockerBuild(image: env.IMAGE_REPO, tag: env.IMAGE_TAG) }
            }
            stage('Trivy Image Scan') {
                steps { trivyScan(type: 'image', target: "${env.IMAGE_REPO}:${env.IMAGE_TAG}") }
            }
            stage('Push Image') {
                steps { dockerPush(registry: registry, image: env.IMAGE_REPO, tag: env.IMAGE_TAG, region: region) }
            }
            stage('Deploy') {
                when { expression { config.get('deploy', true) } }
                steps {
                    composeDeploy(name: config.get('deployName', "${appName}-lib"),
                                  image: "${env.IMAGE_REPO}:${env.IMAGE_TAG}",
                                  port: config.get('deployPort', '5000'))
                }
            }
        }
        post {
            always  { archiveArtifacts artifacts: 'reports/*', allowEmptyArchive: true }
            cleanup { cleanWs(deleteDirs: true, notFailBuild: true) }
        }
    }
}
