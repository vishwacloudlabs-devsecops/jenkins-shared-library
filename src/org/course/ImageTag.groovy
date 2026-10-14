package org.course

/**
 * Builds consistent image references and tags.
 *
 *   def t = new org.course.ImageTag(this)
 *   env.IMAGE_TAG = t.tag()                        // e.g. 42-a1b2c3d
 *   env.IMAGE     = t.repo('dockerhub', 'myuser', 'visitor-counter')
 */
class ImageTag implements Serializable {
    private final def steps

    ImageTag(steps) {
        this.steps = steps
    }

    /** <build number>-<short git commit> */
    String tag() {
        String sha = steps.sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
        return "${steps.env.BUILD_NUMBER}-${sha}"
    }

    /** Full repository for Docker Hub ('owner' = username) or ECR ('owner' = account id). */
    String repo(String registry, String owner, String name, String region = 'us-east-1') {
        if (registry == 'ecr') {
            return "${owner}.dkr.ecr.${region}.amazonaws.com/${name}"
        }
        return "${owner}/${name}"
    }
}
