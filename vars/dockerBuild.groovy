/**
 * Build a Docker image tagged with <tag> and 'latest'.
 *
 * Usage:
 *   dockerBuild(image: 'myuser/visitor-counter', tag: env.IMAGE_TAG)
 */
def call(Map config) {
    if (!config.image || !config.tag) {
        error 'dockerBuild: "image" and "tag" are required'
    }
    String context = config.get('context', '.')
    sh """
      docker build \\
        --build-arg APP_VERSION=${config.tag} \\
        --build-arg GIT_COMMIT=\${GIT_COMMIT:-unknown} \\
        -t ${config.image}:${config.tag} -t ${config.image}:latest ${context}
    """
}
