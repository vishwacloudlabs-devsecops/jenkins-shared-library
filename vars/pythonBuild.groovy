/**
 * Lint and unit-test a Python project, then publish the JUnit report.
 *
 * Usage:
 *   pythonBuild()                                   // defaults
 *   pythonBuild(lintPaths: 'app tests', covPackage: 'app')
 */
def call(Map config = [:]) {
    String requirements = config.get('requirements', 'requirements-dev.txt')
    String lintPaths    = config.get('lintPaths', 'app tests wsgi.py')
    String covPackage   = config.get('covPackage', 'app')

    try {
        sh """
          python3 -m venv .venv
          . .venv/bin/activate
          pip install -q -r ${requirements}
          flake8 ${lintPaths}
          mkdir -p reports
          pytest --junitxml=reports/junit.xml \\
                 --cov=${covPackage} --cov-report=xml:reports/coverage.xml --cov-report=term
        """
    } finally {
        junit allowEmptyResults: true, testResults: 'reports/junit.xml'
    }
}
