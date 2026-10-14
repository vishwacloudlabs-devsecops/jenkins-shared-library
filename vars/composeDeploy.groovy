/**
 * Deploy with Docker Compose and wait until the health endpoint answers.
 *
 * Usage:
 *   composeDeploy(name: 'vc-jenkins', image: "${IMAGE}:${TAG}", port: '5000')
 */
def call(Map config) {
    String name = config.get('name', 'app')
    String port = config.get('port', '5000')
    String host = config.get('host', 'host.docker.internal')
    String path = config.get('healthPath', '/health')
    if (!config.image) { error 'composeDeploy: "image" is required' }

    withEnv(["IMAGE=${config.image}", "APP_NAME=${name}", "APP_PORT=${port}",
             "APP_ENV=${config.get('env', 'production')}"]) {
        sh "docker compose -p ${name} up -d --remove-orphans"
    }

    // Health-check script shipped inside this library (resources/)
    writeFile file: '.health-check.sh', text: libraryResource('org/course/health-check.sh')
    sh "sh .health-check.sh http://${host}:${port}${path} ${config.get('retries', 20)} || " +
       "{ docker compose -p ${name} logs --tail 50; exit 1; }"
}
