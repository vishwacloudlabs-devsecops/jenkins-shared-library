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
