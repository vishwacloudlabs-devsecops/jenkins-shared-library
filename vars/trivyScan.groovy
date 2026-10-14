/**
 * Scan with Trivy. Writes a report to reports/trivy-<type>.txt and fails the
 * build when findings at the gate severity exist.
 *
 * Usage:
 *   trivyScan(type: 'fs')                                        // workspace
 *   trivyScan(type: 'image', target: "${IMAGE}:${TAG}")          // image
 *   trivyScan(type: 'image', target: img, gateSeverity: 'HIGH,CRITICAL')
 */
def call(Map config = [:]) {
    String type         = config.get('type', 'fs')
    String trivyImage   = config.get('trivyImage', 'aquasec/trivy:0.75.0')
    String reportSev    = config.get('reportSeverity', 'HIGH,CRITICAL')
    String gateSev      = config.get('gateSeverity', type == 'image' ? 'CRITICAL' : 'HIGH,CRITICAL')

    String mounts = '-v trivy-cache:/root/.cache/'
    String target
    String extra = ''
    if (type == 'image') {
        if (!config.target) { error 'trivyScan: "target" image is required for type: image' }
        mounts += ' -v /var/run/docker.sock:/var/run/docker.sock'
        target = config.target
        extra  = '--ignore-unfixed'
    } else {
        mounts += ' -v "$WORKSPACE:/src"'
        target = '/src'
        extra  = '--scanners vuln,secret,misconfig --skip-dirs /src/.venv'
    }

    sh """
      mkdir -p reports
      docker run --rm ${mounts} ${trivyImage} ${type} ${extra} \\
        --severity ${reportSev} ${target} > reports/trivy-${type}.txt
      cat reports/trivy-${type}.txt
      docker run --rm ${mounts} ${trivyImage} ${type} ${extra} \\
        --severity ${gateSev} --exit-code 1 --quiet ${target}
    """
}
