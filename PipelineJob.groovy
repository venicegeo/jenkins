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
  def slackToken
  def team
  def repo
  def idx
  def core
  def core_steps
  def script
  def jobject
  def targetbranch
  def envs = [
    stage: [space: 'simulator-stage', domain: 'stage.geointservices.io', api: 'https://api.devops.geointservices.io'],
    int:   [space: 'int',             domain: 'int.geointservices.io',   api: 'https://api.devops.geointservices.io'],
    dev:   [space: 'dev',             domain: 'dev.geointservices.io',   api: 'https://api.devops.geointservices.io'],
    test:  [space: 'test',            domain: 'test.geointservices.io',  api: 'https://api.devops.geointservices.io'],
    prod:  [space: 'prod',            domain: 'geointservices.io',       api: 'https://api.devops.geointservices.io']
  ]
  def pcfvars="""
    case \$space in
      stage) export PCF_SPACE=${envs.stage.space}; export PCF_DOMAIN=${envs.stage.domain}; export PCF_API=${envs.stage.api}; export PCF_ORG=piazza ;;
      int)   export PCF_SPACE=${envs.int.space}  ; export PCF_DOMAIN=${envs.int.domain}  ; export PCF_API=${envs.int.api}  ; export PCF_ORG=piazza ;;
      dev)   export PCF_SPACE=${envs.dev.space}  ; export PCF_DOMAIN=${envs.dev.domain}  ; export PCF_API=${envs.dev.api}  ; export PCF_ORG=piazza ;;
      test)  export PCF_SPACE=${envs.test.space} ; export PCF_DOMAIN=${envs.test.domain} ; export PCF_API=${envs.test.api} ; export PCF_ORG=piazza ;;
      prod)  export PCF_SPACE=${envs.prod.space} ; export PCF_DOMAIN=${envs.prod.domain} ; export PCF_API=${envs.prod.api} ; export PCF_ORG=piazza ;;
    esac
  """
  def appvars="""
    root=\$(pwd -P)

    [ ! -f \$root/ci/vars.sh ] && echo "No vars.sh" && exit 1
    source \$root/ci/vars.sh

    [[ -z "\$APP" || -z "\$EXT" ]] && echo "APP and EXT must be defined" && exit 1

    version=\$(git describe --long --tags --always)
    artifact=\$APP-\$version.\$EXT
    cfhostname=\$(echo \$APP-\$version | sed 's/\\./-/g')
  """
  def cfauth="""
    root=\$(pwd -P)

    export CF_HOME=\$root

    set +x
    cf api \$PCF_API > /dev/null
    cf auth "\$CF_USER" "\$CF_PASSWORD" > /dev/null
    cf target -o \$PCF_ORG -s \$PCF_SPACE > /dev/null
    set -x
  """

  def base() {
    this.jobject.with {

      properties {
        githubProjectUrl "https://github.com/venicegeo/${this.repo}"
      }

      parameters {
        choiceParam('space', new ArrayList<String>(this.envs.keySet()),'PCF Space to target')
        stringParam('revision', 'latest', 'commit sha, git branch or tag to build (default: latest revision)')
      }

      scm {
        git {
          remote {
            github "venicegeo/${this.repo}"
          }
          branch("${this.targetbranch}")
        }
      }

      steps {
        shell("""
          git clean -xffd
          [ "\$revision" != "latest" ] && git checkout \$revision
          [ -f ./ci/${this.script}.sh ] || { echo "noop"; exit; }
          chmod 700 ./ci/${this.script}.sh
          ./ci/${this.script}.sh
          exit \$?
        """)
      }

      publishers {
        slackNotifications {
          projectChannel "#jenkins"
          integrationToken this.slackToken
          configure { node ->
            teamDomain "venicegeo"
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
            customMessage "    revision: `\$revision`\n      space: `\$space`\n      commit sha: `\$GIT_COMMIT`"
          }
        }
      }

      logRotator { numToKeep 30 }
    }

    return this
  }

  def trigger() {
    this.jobject.with {
      triggers {
        githubPush()
      }
    }
    return this
  }

  def downstream(childname) {
    this.jobject.with {
      publishers {
        downstreamParameterized {
          trigger("${this.team}/${this.repo}/${childname}") {
            condition('SUCCESS')
            parameters {
              predefinedProp('revision', '$revision')
              predefinedProp('space', '$space')
            }
          }
        }
      }
    }

    return this
  }

  def archive() {
    this.jobject.with {
      steps {
        shell("""
          ${this.appvars}

          mv \$root/\$APP.\$EXT \$artifact

          mvn dependency:get \
            -DremoteRepositories="nexus::default::https://nexus.devops.geointservices.io/content/repositories/${this.team.capitalize()}" \
            -DrepositoryId=nexus \
            -DartifactId=\$APP \
            -DgroupId=org.venice.${this.team}\
            -Dpackaging=\$EXT \
            -Dtransitive=false \
            -Dversion=\$version \
          && { echo "artifact already exists! Noop!"; exit 0; } || true

          # pom?
          [ -f \$root/pom.xml ] && genpom=false || genpom=false

          # push artifact to nexus
          mvn deploy:deploy-file \
            -Durl="https://nexus.devops.geointservices.io/content/repositories/${this.team.capitalize()}" \
            -DrepositoryId=nexus \
            -Dfile=\$artifact \
            -DgeneratePom=\$genpom \
            -DgroupId=org.venice.${this.team}\
            -DartifactId=\$APP \
            -Dversion=\$version \
            -Dpackaging=\$EXT

          rm \$artifact
        """)
      }
    }

    return this
  }

  def stage() {

    if (this.core) {
      this.core_steps[this.repo] = "${this.repo}/${this.idx}-${this.script}"
    }

    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('CF_USER', 'CF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
        }
      }
      steps {
        shell("""
          ${this.appvars}

          mvn dependency:get \
            -DremoteRepositories="nexus::default::https://nexus.devops.geointservices.io/content/repositories/${this.team.capitalize()}" \
            -DrepositoryId=nexus \
            -DartifactId=\$APP \
            -DgroupId=org.venice.${this.team}\
            -Dpackaging=\$EXT \
            -Dtransitive=false \
            -Dversion=\$version \
            -Ddest=\$root/\$APP.\$EXT

          [ "bin" = "\$EXT" ] && chmod 755 \$root/\$APP.\$EXT
          [ "tar.gz" = "\$EXT" ] && tar -xzf \$root/\$APP.\$EXT

          [ -f \$root/\$APP.\$EXT ] || exit 1

          ${this.pcfvars}
          ${this.cfauth}

          set +e

          [ -f manifest.\$space.yml ] && manifest=manifest.\$space.yml || manifest=manifest.jenkins.yml

          cf push \$APP-\$version -f \$manifest --hostname \$cfhostname -d \$PCF_DOMAIN

          if [ \$? != 0 ]; then
            cf delete \$APP-\$version -f -r
            rm \$root/\$APP.\$EXT
            exit 1
          fi

          rm \$root/\$APP.\$EXT
        """)
      }
    }

    return this
  }

  def deploy() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('CF_USER', 'CF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
        }
      }
      steps {
        shell("""
          ${this.appvars}
          ${this.pcfvars}
          ${this.cfauth}

          set +e

          legacy=`cf routes | grep "\$APP " | awk '{print \$4}'`
          target=\$APP-\$version
          [ "\$target" = "\$legacy" ] && { echo "nothing to do."; exit 0; }
          cf map-route \$APP-\$version \$PCF_DOMAIN --hostname \$APP
          s=\$?
          [ -n "\$legacy" ] && cf unmap-route "\$legacy" \$PCF_DOMAIN --hostname \$APP
          [ -n "\$legacy" ] && cf delete "\$legacy" -f -r || exit \$s
        """)
      }
    }

    return this
  }

  def blackbox() {
    this.jobject.with {
      configure { project ->
        project / buildWrappers << 'jenkins.plugins.nodejs.tools.NpmPackagesBuildWrapper' {
          nodeJSInstallationName "Node 5.7.0"
        }
      }

      steps {
        shell('npm install -g newman@2')
      }
    }

    return this
  }
}
