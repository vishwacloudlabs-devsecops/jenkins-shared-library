# course-shared-lib — Jenkins Shared Library

Reusable pipeline code for the course (Lab 15).

## Register in Jenkins

Manage Jenkins → System → **Global Trusted Pipeline Libraries** → Add

| Field | Value |
|-------|-------|
| Name | `course-shared-lib` |
| Default version | `main` |
| Retrieval method | Modern SCM → Git → this repository |

## Steps (`vars/`)

| Step | Purpose |
|------|---------|
| `pythonBuild()` | venv, flake8, pytest with coverage, JUnit |
| `trivyScan(type: 'fs' \| 'image', target: ...)` | Trivy report + severity gate |
| `dockerBuild(image:, tag:)` | Build with version/commit labels |
| `dockerPush(registry: 'dockerhub' \| 'ecr', image:, tag:)` | Push to Docker Hub or Amazon ECR |
| `composeDeploy(name:, image:, port:)` | Docker Compose deploy + health check |
| `standardPythonPipeline(...)` | The complete pipeline for Python services |

## Usage

```groovy
@Library('course-shared-lib@v1.0.0') _

standardPythonPipeline(
    appName:       'visitor-counter',
    dockerHubUser: 'your-dockerhub-user',
    deployName:    'vc-lib',
    deployPort:    '5002'
)
```

`src/org/course/ImageTag.groovy` computes `<build>-<sha>` tags; `resources/org/course/health-check.sh`
is loaded with `libraryResource`.

## Test library changes (Day 15)

With JDK 21 and Maven 3.9 installed, run `mvn test` from this directory.
The two JenkinsPipelineUnit tests exercise `pythonBuild`: configured lint/coverage
commands and report publication, including publication when the shell step fails.
Dependencies are pinned in `pom.xml`; the first run needs Maven Central and the
Jenkins releases repository. Results appear in `target/surefire-reports/`.

These tests mock Jenkins steps. Also lint the consuming Declarative Jenkinsfile
and run a real build on a test controller before publishing a new library tag.
Use `mvn clean` to remove local reports and compiled tests.
