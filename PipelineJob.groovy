// Copyright 2016, RadiantBlue Technologies, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

class PipelineJob {
  def project
  def step
  def job
  def branch
  def cfapi
  def cfdomain

  def base() {
    this.job.with {
      properties {
        githubProjectUrl "https://github.com/venicegeo/${this.project}"
      }

      scm {
        git {
          remote {
            github "venicegeo/${this.project}"
          }
          branch("${this.branch}")
        }
      }

      steps {
        shell("""
          git clean -xffd
          [ -f ./scripts/${this.step}.sh ] || { echo "noop"; exit; }
          chmod 700 ./scripts/${this.step}.sh
          ./scripts/${this.step}.sh
          exit \$?
        """)
      }

      logRotator { numToKeep 30 }

      wrappers {
        colorizeOutput()
      }
    }

    return this
  }

  def trigger() {
    return this.job.with {
      triggers {
        githubPush()
      }
    }
  }

  def deliver() {
    this.job.with {
      configure { project ->
        project / publishers << 'com.hpe.cloudfoundryjenkins.CloudFoundryPushPublisher' {
          target "${this.cfapi}"
          organization 'piazza'
          cloudSpace 'simulator-stage'
          credentialsId '6ad30d14-e498-11e5-9730-9a79f06e9478'
          selfSigned false
          resetIfExists true
          pluginTimeout 120
          servicesToCreate ''
          appURIs ''
          manifestChoice {
            value 'manifestFile'
            manifestFile 'manifest.yml'
            memory 0
            instances 0
            noRoute false
          }
        }
      }
    }

    return this
  }
}
