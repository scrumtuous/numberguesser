// ============================================================================
// Jenkinsfile - Number Guesser (training / teaching pipeline)
// ----------------------------------------------------------------------------
// This pipeline is intentionally written as a *learning tool*. Real production
// pipelines are usually stricter, but here every stage is designed so that:
//
//   1. Nothing halts the whole pipeline just because a tool/infra piece is
//      missing on the agent (Docker, AWS CLI, OpenTofu, a downstream Jenkins
//      job, etc). Missing tools => the stage is skipped and marked UNSTABLE,
//      not FAILED, so learners can see every stage execute every run.
//   2. Anything that talks to "real" infrastructure (Docker Hub, AWS, cloud
//      resources via OpenTofu) asks a Yes/No question first, with a short
//      timeout that defaults to "skip" if nobody answers (e.g. when the job
//      is triggered automatically instead of run by hand).
//   3. The pipeline works whether the agent is Windows or Linux, using
//      Jenkins' built-in isUnix() check instead of hardcoding bat/sh.
// ============================================================================

// ---- Helper functions (plain Groovy, available to every stage below) ------

// Run a shell command on whatever OS the agent happens to be.
def runCmd(String command) {
    if (isUnix()) {
        sh command
    } else {
        bat command
    }
}

// Return true/false instead of throwing, so we can decide to skip gracefully.
def commandExists(String tool) {
    if (isUnix()) {
        return sh(script: "command -v ${tool}", returnStatus: true) == 0
    } else {
        return bat(script: "where ${tool}", returnStatus: true) == 0
    }
}

// Run a command and return its trimmed stdout, cross-platform.
def cmdOutput(String command) {
    if (isUnix()) {
        return sh(script: command, returnStdout: true).trim()
    } else {
        return bat(script: "@${command}", returnStdout: true).trim()
    }
}

// Ask a Yes/No question before running an optional stage. If nobody answers
// within `timeoutSeconds` (e.g. an unattended/automated build), fall back to
// `defaultValue` instead of blocking forever.
def askToRun(String question, boolean defaultValue, int timeoutSeconds = 20) {
    def proceed = defaultValue
    try {
        timeout(time: timeoutSeconds, unit: 'SECONDS') {
            proceed = input(
                message: question,
                ok: 'Confirm',
                parameters: [booleanParam(name: 'PROCEED', defaultValue: defaultValue, description: 'Uncheck to skip this optional stage')]
            )
        }
    } catch (err) {
        echo "No answer received in time - defaulting to ${defaultValue ? 'RUN' : 'SKIP'} for: \"${question}\""
    }
    return proceed
}

pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 1, unit: 'HOURS')
    }

    environment {
        // Replace these with real values when you have somewhere to push/deploy to.
        DOCKER_IMAGE = 'scrumtuous/numberguesser'
        AWS_CLUSTER  = 'numberguesser-cluster'
        AWS_SERVICE  = 'numberguesser-service'
    }

    stages {

        stage('Tool & Environment Check') {
            parallel {
                stage('Log Tool Versions') {
                    steps {
                        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                            script {
                                [['mvn', 'mvn --version'], ['git', 'git --version'], ['java', 'java -version']].each { tool, cmd ->
                                    if (commandExists(tool)) {
                                        runCmd(cmd)
                                    } else {
                                        echo "'${tool}' was not found on this agent - skipping version check."
                                    }
                                }
                            }
                        }
                    }
                }

                stage('Check for POM') {
                    steps {
                        catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                            script {
                                if (fileExists('pom.xml')) {
                                    echo 'pom.xml found - this looks like a Maven project.'
                                } else {
                                    echo 'pom.xml NOT found - later Maven stages will be skipped.'
                                }
                            }
                        }
                    }
                }
            }
        }

        stage('Build with Maven') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (commandExists('mvn') && fileExists('pom.xml')) {
                            runCmd('mvn -B compile')
                        } else {
                            echo 'Maven (or pom.xml) not available - skipping compile.'
                        }
                    }
                }
            }
        }

        stage('Run Tests') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (commandExists('mvn') && fileExists('pom.xml')) {
                            runCmd('mvn -B test')
                        } else {
                            echo 'Maven (or pom.xml) not available - skipping tests.'
                        }
                    }
                }
            }
        }

        stage('Package Application') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (commandExists('mvn') && fileExists('pom.xml')) {
                            // This is a WAR project (see pom.xml packaging), not Spring Boot,
                            // so we just run the normal package phase.
                            runCmd('mvn -B package')
                        } else {
                            echo 'Maven (or pom.xml) not available - skipping package step.'
                        }
                    }
                }
            }
        }

        stage('Run Static Code Analysis') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (!askToRun('Run static code analysis (Checkstyle, PMD & SpotBugs), published via the Warnings Next Generation plugin?', false)) {
                            echo 'Skipping static code analysis (optional stage).'
                        } else if (!(commandExists('mvn') && fileExists('pom.xml'))) {
                            echo 'Maven (or pom.xml) not available - skipping static code analysis.'
                        } else {
                          def analysisTools = [
                                [name: 'Checkstyle', goal: 'org.apache.maven.plugins:maven-checkstyle-plugin:3.3.1:checkstyle', report: 'target/checkstyle-result.xml'],
                                [name: 'PMD',        goal: 'org.apache.maven.plugins:maven-pmd-plugin:3.21.2:pmd',              report: 'target/pmd.xml'],
                                [name: 'SpotBugs',   goal: 'com.github.spotbugs:spotbugs-maven-plugin:4.8.3.1:spotbugs',       report: 'target/spotbugsXml.xml']
                            ]

                            analysisTools.each { t ->
                                try {
                                    runCmd("mvn -B ${t.goal}")
                                } catch (err) {
                                    echo "${t.name} did not complete (${err.message}) - continuing with the other analyzers."
                                }
                            }

                            try {
                                recordIssues(
                                    tools: [
                                        checkStyle(pattern: 'target/checkstyle-result.xml'),
                                        pmdParser(pattern: 'target/pmd.xml'),
                                        spotBugs(pattern: 'target/spotbugsXml.xml'),
                                        // These three need no separate Maven goal or report file at
                                        // all - they're built straight into the Warnings NG plugin
                                        // and parse either this build's own console log, or the
                                        // source tree, on the fly.
                                        java(),                                        // javac compiler warnings from the console log
                                        mavenConsole(),                                // Maven's own errors/warnings from the console log
                                        taskScanner(                                   // TODO/FIXME comments in the source
                                            includePattern: '**/*.java',
                                            highTags: 'FIXME',
                                            normalTags: 'TODO'
                                        )
                                    ],
                                    qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]]
                                )
                            } catch (err) {
                                echo "Could not publish results - is the Warnings Next Generation plugin (warnings-ng) installed on this Jenkins server? (${err.message})"
                            }
                        }
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        env.DOCKER_IMAGE_BUILT = 'false'
                        if (askToRun('Build a Docker image? (requires Docker installed on this agent)', true)) {
                            if (commandExists('docker')) {
                                runCmd("docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} .")
                                env.DOCKER_IMAGE_BUILT = 'true'
                            } else {
                                echo 'Docker CLI not found on this agent - skipping Docker build. (Install Docker on the agent, or point this pipeline at a Docker-enabled node, to unlock this stage.)'
                            }
                        } else {
                            echo 'Skipping Docker build (optional stage).'
                        }
                    }
                }
            }
        }

        stage('Smoke Test Docker Image') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (env.DOCKER_IMAGE_BUILT != 'true') {
                            echo 'No Docker image was built in the previous stage - skipping smoke test.'
                        } else if (!commandExists('docker')) {
                            echo 'Docker CLI not found on this agent - skipping smoke test.'
                        } else {
                            // Reuses the image's own HEALTHCHECK (see Dockerfile) as the
                            // smoke test - no host port needs to be published for this,
                            // Docker runs the HEALTHCHECK command inside the container.
                            def containerName = "numberguesser-smoketest-${BUILD_NUMBER}"
                            try {
                                runCmd("docker run -d --rm --name ${containerName} ${DOCKER_IMAGE}:${BUILD_NUMBER}")
                                def healthy = false
                                for (int i = 0; i < 12; i++) {
                                    def status = cmdOutput("docker inspect --format=\"{{.State.Health.Status}}\" ${containerName}")
                                    echo "Container health status: ${status}"
                                    if (status == 'healthy') {
                                        healthy = true
                                        break
                                    }
                                    sleep(time: 5, unit: 'SECONDS')
                                }
                                if (!healthy) {
                                    error 'Smoke test failed - container did not report healthy in time.'
                                } else {
                                    echo 'Smoke test passed - container is healthy.'
                                }
                            } finally {
                                runCmd("docker stop ${containerName}")
                            }
                        }
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (env.DOCKER_IMAGE_BUILT != 'true') {
                            echo 'No Docker image was built in the previous stage - skipping push.'
                        } else if (askToRun('Push the Docker image to Docker Hub? (requires the agent to be logged in via `docker login`)', false)) {
                            if (commandExists('docker')) {
                                runCmd("docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}")
                            } else {
                                echo 'Docker CLI not found on this agent - skipping push.'
                            }
                        } else {
                            echo 'Skipping Docker push (optional stage).'
                        }
                    }
                }
            }
        }

        stage('Provision Infrastructure with OpenTofu') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (askToRun('Provision external infrastructure with OpenTofu? (requires the "tofu" CLI + cloud credentials on this agent)', false)) {
                            if (!commandExists('tofu')) {
                                echo 'OpenTofu ("tofu") CLI not found on this agent - skipping infrastructure provisioning.'
                            } else if (!fileExists('infra')) {
                                echo 'No ./infra directory with OpenTofu (.tf) configuration was found in this repo - nothing to provision. (This is a placeholder stage for teaching purposes; a real project would keep its .tf files under ./infra.)'
                            } else {
                                dir('infra') {
                                    runCmd('tofu init')
                                    runCmd('tofu plan -out=tfplan')
                                    if (askToRun('Apply the OpenTofu plan? This can create real cloud resources.', false)) {
                                        runCmd('tofu apply -auto-approve tfplan')
                                    } else {
                                        echo 'Skipping apply - plan only.'
                                    }
                                }
                            }
                        } else {
                            echo 'Skipping infrastructure provisioning (optional stage).'
                        }
                    }
                }
            }
        }

        stage('Deploy to AWS') {
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    script {
                        if (askToRun('Deploy to AWS ECS? (requires the AWS CLI + credentials configured on this agent)', false)) {
                            if (commandExists('aws')) {
                                runCmd("aws ecs update-service --cluster ${AWS_CLUSTER} --service ${AWS_SERVICE} --force-new-deployment")
                            } else {
                                echo 'AWS CLI not found on this agent - skipping deployment.'
                                writeFile(file: 'deployment.txt', text: 'We did not deploy - AWS CLI unavailable.')
                            }
                        } else {
                            echo 'Skipping AWS deployment (optional stage).'
                            writeFile(file: 'deployment.txt', text: 'We did not deploy - user chose to skip.')
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished with result: ${currentBuild.currentResult}"
        }
    }
}
