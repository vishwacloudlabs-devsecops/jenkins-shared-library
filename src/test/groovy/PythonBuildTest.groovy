import com.lesfurets.jenkins.unit.BasePipelineTest
import org.junit.Before
import org.junit.Test

class PythonBuildTest extends BasePipelineTest {
    List<String> commands = []
    List<Map> reports = []
    boolean failShell = false

    @Before
    void setUp() {
        super.setUp()
        scriptRoots = ['.']
        helper.registerAllowedMethod('sh', [String]) { String command ->
            commands << command
            if (failShell) throw new RuntimeException('simulated test failure')
        }
        helper.registerAllowedMethod('junit', [Map]) { Map config -> reports << config }
    }

    @Test
    void runsConfiguredTestsAndPublishesReport() {
        loadScript('vars/pythonBuild.groovy').call(lintPaths: 'app', covPackage: 'app')
        assert commands.size() == 1
        assert commands[0].contains('flake8 app')
        assert commands[0].contains('--cov=app')
        assert reports*.testResults == ['reports/junit.xml']
    }

    @Test
    void publishesReportAndPropagatesShellFailure() {
        failShell = true
        try {
            loadScript('vars/pythonBuild.groovy').call()
            org.junit.Assert.fail('Expected the shell failure to propagate')
        } catch (RuntimeException failure) {
            assert failure.message == 'simulated test failure'
        }
        assert reports*.testResults == ['reports/junit.xml']
    }
}
