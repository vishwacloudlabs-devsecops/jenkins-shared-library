/**
 * Push <image>:<tag> and <image>:latest to Docker Hub or Amazon ECR.
 *
 * Usage:
 *   dockerPush(image: 'myuser/visitor-counter', tag: env.IMAGE_TAG)       // Docker Hub
 *   dockerPush(registry: 'ecr', image: ecrRepo, tag: env.IMAGE_TAG,
 *              credentialsId: 'aws-ecr-creds', region: 'us-east-1')
 */
def call(Map config) {
    String registry = config.get('registry', 'dockerhub')
    String image    = config.image
    String tag      = config.tag
    if (!image || !tag) { error 'dockerPush: "image" and "tag" are required' }

    if (registry == 'ecr') {
        String region = config.get('region', 'us-east-1')
        String host   = image.tokenize('/')[0]
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                          credentialsId: config.get('credentialsId', 'aws-ecr-creds')]]) {
            sh """
              aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${host}
              docker push ${image}:${tag}
              docker push ${image}:latest
            """
        }
    } else {
        withCredentials([usernamePassword(credentialsId: config.get('credentialsId', 'dockerhub-creds'),
                                          usernameVariable: 'DH_USER', passwordVariable: 'DH_TOKEN')]) {
            sh """
              echo "\$DH_TOKEN" | docker login -u "\$DH_USER" --password-stdin
              docker push ${image}:${tag}
              docker push ${image}:latest
            """
        }
    }
}
