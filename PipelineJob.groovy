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

      publishers {
        slackNotifications {
          configure { node ->
            teamDomain "https://venicegeo.slack.com"
            token credentialsId("53c859fe-e776-11e5-9730-9a79f06e9478")
            room "jenkins"
            startNotification false
            notifySuccess false
            notifyAborted true
            notifyNotBuilt true
            notifyUnstable true
            notifyFailure true
            notifyBackToNormal true
            notifyRepeatedFailure true
            includeTestSummary false
            showCommitList false
            includeCustomMessage true
            customMessage "<$GIT_COMMIT>"
          }
        }
      }

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
          [ -f ./ci/${this.step}.sh ] || { echo "noop"; exit; }
          chmod 700 ./ci/${this.step}.sh
          ./ci/${this.step}.sh
          exit \$?
        """)
      }

      logRotator { numToKeep 30 }
    }

    return this
  }

  def trigger() {
    this.job.with {
      triggers {
        githubPush()
      }
    }

    return this
  }

  def archive() {
    this.job.with {
      steps {
        shell("""
          root=\$(pwd -P)

          [ ! -f \$root/ci/vars.sh ] && echo "No vars.sh" && exit 1
          source \$root/ci/vars.sh

          [ ! -f \$ARTIFACT ] && echo "No artifact?" && exit 1

          # pom?
          [ -f \$root/pom.xml ] && genpom=false || genpom=false

          # push artifact to nexus
          mvn deploy:deploy-file \
            -Durl="https://nexus.devops.geointservices.io/content/repositories/Piazza" \
            -DrepositoryId=nexus \
            -Dfile=\$ARTIFACT \
            -DgeneratePom=\$genpom \
            -DgroupId=io.piazzageo \
            -DartifactId=\$APP \
            -Dversion=\$VERSION \
            -Dpackaging=\$EXT
        """)
      }
    }

    return this
  }

  def deliver() {
    this.job.with {
      steps {
        shell("""
          root=\$(pwd -P)

          [ ! -f \$root/ci/vars.sh ] && echo "No vars.sh" && exit 1
          source \$root/ci/vars.sh

          [ -f \$root/\$APP.\$EXT ] && exit

          mvn dependency:get \
            -DremoteRepositories=nexus::default::"https://nexus.devops.geointservices.io/content/repositories/Piazza" \
            -DrepositoryId=nexus \
            -DartifactId=\$APP \
            -DgroupId=io.piazzageo \
            -Dpackaging=\$EXT \
            -Dtransitive=false \
            -Dversion=\$VERSION

          mvn dependency:copy \
            -Dartifact=io.piazzageo:\$APP:\$VERSION:\$EXT \
            -DoutputDirectory=\$root

          mv \$root/\$ARTIFACT \$root/\$APP.\$EXT
        """)
      }

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
            manifestFile 'ci/manifest.yml'
            memory 0
            instances 0
            noRoute false
          }
        }
      }
    }

    return this
  }

  def deploy() {
    this.job.with {
      steps {
        shell("""
          legacy=`cf routes | grep '${this.project} ' | awk '{print \$4}'`
          target=${this.project}-`git rev-parse HEAD`
          [ "\$target" = "\$legacy" ] && { echo "nothing to do."; exit 0; }
          cf map-route ${this.project}-`git rev-parse HEAD` ${this.cfdomain} -n ${this.project}
          s=\$?
          [ -n "\$legacy" ] && cf delete -f \$legacy || exit \$s
        """)
      }
    }

    return this
  }
}
