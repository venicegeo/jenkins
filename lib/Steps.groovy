package lib

class Steps {
  def jobject
  def config
  def jobname
  def override = ""

  def init() {
    this._pcf_env="""
      override="${this.override}"
      [ -z "\$override" ] && echo "target_domain: \$target_domain" || target_domain=\$override
      case \$target_domain in
        int.geointservices.io)    export PCF_SPACE=${this.config.envs.int.space}   ; export PCF_DOMAIN=${this.config.envs.int.domain}   ; export PCF_API=${this.config.envs.int.api}   ; export PCF_ORG=${this.config.pcf_org} ;;
        stage.geointservices.io)  export PCF_SPACE=${this.config.envs.stage.space} ; export PCF_DOMAIN=${this.config.envs.stage.domain} ; export PCF_API=${this.config.envs.stage.api} ; export PCF_ORG=${this.config.pcf_org} ;;
        dev.geointservices.io)    export PCF_SPACE=${this.config.envs.dev.space}   ; export PCF_DOMAIN=${this.config.envs.dev.domain}   ; export PCF_API=${this.config.envs.dev.api}   ; export PCF_ORG=${this.config.pcf_org} ;;
        test.geointservices.io)   export PCF_SPACE=${this.config.envs.test.space}  ; export PCF_DOMAIN=${this.config.envs.test.domain}  ; export PCF_API=${this.config.envs.test.api}  ; export PCF_ORG=${this.config.pcf_org} ;;
        geointservices.io)        export PCF_SPACE=${this.config.envs.prod.space}  ; export PCF_DOMAIN=${this.config.envs.prod.domain}  ; export PCF_API=${this.config.envs.prod.api}  ; export PCF_ORG=${this.config.pcf_org} ;;
        *)                        export PCF_SPACE=${this.config.envs.int.space}   ; export PCF_DOMAIN=${this.config.envs.int.domain}   ; export PCF_API=${this.config.envs.int.api}   ; export PCF_ORG=${this.config.pcf_org} ;;
      esac
    """

    return this
  }

  def job_script() {
    this.jobject.with {
      steps {
        shell(this._job_script())
      }
    }

    return this
  }

  def git_checkout() {
    this.jobject.with {
      steps {
        shell(this._git_script())
      }
    }

    return this
  }

  def create_properties_file() {
    this.jobject.with {
      steps {
        shell(this._create_properties_file_script())
      }
    }

    return this
  }

  def pass_properties_file() {
    this.jobject.with {
      steps {
        shell(this._pass_properties_file_script())
      }
    }

    return this
  }

  def gh_trigger() {
    this.jobject.with {
      triggers {
        githubPush()
      }
    }

    return this
  }

  def gh_write() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          file('GIT_KEY', '4C2105AE-41EB-42A0-963F-5CE91B814832')
        }
      }
      steps {
        shell(this._github_write_script())
      }
    }

    return this
  }
  
  def archive() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('NAQUINKJ_USER', 'NAQUINKJ_PASS', '4728add1-a64f-4bd3-8069-d5312368c8ea')
        }
      }
      steps {
        shell(this._archive_script())
      }
    }

    return this
  }

  def jks() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          file('JKS', 'ca8591a7-fc1f-4b6d-808e-c9944c9bf4f8')
          string('JKS_PASSPHRASE', 'ff7148c6-2855-4f3d-bd2e-3aa296b09d98')
          string('PZ_PASSPHRASE', 'da3092c4-d13d-4078-ab91-a630c61547aa')
        }
      }
    }

    return this
  }

  def ionchannel_pom() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          string('IONCHANNEL_SECRET_KEY', '20E6021F-B1DE-4FF5-A53B-D995324775B0')
        }
      }

      steps {
        shell(this._ionchannel_pom_script())
      }
    }

    return this
  }

  def fortify() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          string('THREADFIX_KEY', '978C467A-2B26-47AE-AD2F-4AFD5A4AF695')
        }
      }
      // # NOTE: Fortify is only installed on sl61-be2c3fee
      configure { project ->
        project << assignedNode('sl61-be2c3fee')
        project << canRoam('false')
      }
      steps {
        shell """
          src=\$(find src/main -name *.java)
          [ ! -f \$src ] && echo "Source not found." && exit 1
          /bin/mvn install:install-file -Dfile=pom.xml -DpomFile=pom.xml 
          /opt/hp_fortify_sca/bin/sourceanalyzer -b \${BUILD_NUMBER} \$src
          /opt/hp_fortify_sca/bin/sourceanalyzer -b \${BUILD_NUMBER}  -scan -Xmx1G -f fortifyResults-\${BUILD_NUMBER}.fpr
          # All Piazza projects are id 10 in threadfix ie applications/10 in the curl
          /bin/curl -v --insecure -H 'Accept: application/json' -X POST --form file=@fortifyResults-\${BUILD_NUMBER}.fpr https://threadfix.devops.geointservices.io/rest/applications/10/upload?apiKey=\${THREADFIX_KEY}
          #/opt/hp_fortify_sca/bin/ReportGenerator -format pdf -f ${this.config.gh_repo}-fortify-\${BUILD_NUMBER}.pdf -source fortifyResults-\${BUILD_NUMBER}.fpr"
        """
      }
    }
  }

  def sonar() {
    this.jobject.with {
      configure { project ->
        project << assignedNode('sl55')
        project << canRoam('false')
      }

      wrappers {
        credentialsBinding {
          string('REDMINE_KEY', 'C0C13D9C-C21F-4DDE-9AC9-6965E31E54B7')
        }
      }

      environmentVariables {
        env('APP', "${this.config.gh_repo}")
        env('GH_ORG', "${this.config.gh_org}")
        env('JENKINS_ORG', "${this.config.jenkins_org}")
        env('TEAM', "${this.config.team}")
      }

      steps {
        sonarRunnerBuilder {
          installationName "DevOps Sonar"
          sonarScannerName "DevOps Sonar"
          properties """
sonar.redmine.api-access-key=\${REDMINE_KEY}
sonar.projectKey=\${JENKINS_ORG}:\${TEAM}:\${APP}
sonar.projectName=\${JENKINS_ORG}:\${TEAM}:\${APP}
sonar.projectVersion=\${GIT_COMMIT}
sonar.links.homepage=https://redmine.devops.geointservices.io/projects/\${TEAM}
sonar.links.ci=\${JOB_URL}
sonar.links.issue=https://redmine.devops.geointservices.io/projects/\${TEAM}/issues
sonar.links.scm=https://github.com/\${GH_ORG}/\${APP}
sonar.links.scm_dev=https://github.com/\${GH_ORG}/\${APP}.git
sonar.redmine.url=https://redmine.devops.geointservices.io
          """
          jdk "JDK 1.8uLATEST"
          task " "
          additionalArguments " "
          javaOpts " "
          project " "
        }
      }
    }

    return this
  }

  def cf_push() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('PCF_USER', 'PCF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
        }
      }
      steps {
        shell(this._cf_push_script())
      }
    }

    return this
  }

  def cf_push_release() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('PCF_USER', 'PCF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
          file('GIT_KEY', '4C2105AE-41EB-42A0-963F-5CE91B814832')
        }
      }
      steps {
        shell(this._cf_push_release_script())
      }
    }

    return this
  }

  def cf_set_version() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('PCF_USER', 'PCF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
        }
      }
      steps {
        shell(this._cf_set_version_script())
      }
    }

    return this
  }

  def cf_push_int() {
    this.override = "int.geointservices.io"
    this.init()
    this.cf_push()

    return this
  }

  def cf_push_stage() {
    this.override = "stage.geointservices.io"
    this.init()
    this.cf_push()

    return this
  }

  def cf_release_int() {
    this.override = "int.geointservices.io"
    this.init()
    this.cf_push_release()
    this.cf_bg_deploy()

    return this
  }

  def cf_release_stage() {
    this.override = "stage.geointservices.io"
    this.init()
    this.cf_push_release()
    this.cf_bg_deploy()

    return this
  }

  def cf_release_test() {
    this.override = "test.geointservices.io"
    this.init()
    this.cf_push_release()
    this.cf_bg_deploy()

    return this
  }

  def cf_release_prod() {
    this.override = "geointservices.io"
    this.init()
    this.cf_push_release()
    this.cf_bg_deploy()

    return this
  }

  def cf_promote_to_test() {
    this.override = "stage.geointservices.io"
    this.init()
    this.cf_set_version()

    this.override = "test.geointservices.io"
    this.init()
    this.cf_push()
    this.cf_bg_deploy()

    return this
  }

  def cf_promote_to_prod() {
    this.override = "stage.geointservices.io"
    this.init()
    this.cf_set_version()

    this.override = "geointservices.io"
    this.init()
    this.cf_push()
    this.cf_bg_deploy()

    return this
  }

  def cf_promote_to_stage() {
    this.override = "int.geointservices.io"
    this.init()
    this.cf_set_version()

    this.override = "stage.geointservices.io"
    this.init()
    this.cf_push()
    this.cf_bg_deploy()

    return this
  }

  def cf_hotfix_prod() {
    this.override = "geointservices.io"
    this.init()
    this.cf_push()
    this.cf_bg_deploy()

    return this
  }

  def cf_bg_deploy() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('PCF_USER', 'PCF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
          file('GIT_KEY', '4C2105AE-41EB-42A0-963F-5CE91B814832')
        }
      }
      steps {
        shell(this._cf_bg_deploy_script())
      }
    }

    return this
  }

  def cf_bg_deploy_int() {
    this.override = "int.geointservices.io"
    this.init()
    this.cf_bg_deploy()

    return this
  }

  def cf_bg_deploy_stage() {
    this.override = "stage.geointservices.io"
    this.init()
    this.cf_bg_deploy()

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
        shell('npm install -g newman@2 karma-cli')
      }
    }

    return this
  }

  private String _pcf_env

  private String _app_env="""
    root=\$(pwd -P)

    [ -f \$root/ci/vars.sh ] && source \$root/ci/vars.sh

    [[ -z "\$APP" || -z "\$EXT" ]] && echo "APP and EXT must be defined" && exit 1

    version=\$(git describe --long --tags --always)
    artifact=\$APP-\$version.\$EXT
    cfhostname=\$(echo \$APP-\$version | sed 's/\\./-/g')
  """

  private String _cf_auth="""
    root=\$(pwd -P)

    export CF_HOME=\$root

    set +x
    export HISTFILE=/dev/null
    cf api \$PCF_API > /dev/null
    cf auth "\$PCF_USER" "\$PCF_PASSWORD" > /dev/null
    cf target -o \$PCF_ORG -s \$PCF_SPACE > /dev/null
    set -x
  """

  def _git_script() {
    return """
      df -lH .
      git clean -xffd
      [ -z \$component_revision ] && component_revision=latest || echo \$component_revision
      [ "\$component_revision" != "latest" ] && git checkout \$component_revision || echo "using latest component_revision"
    """
  }

  def _job_script() {
    return """
      ${this._pcf_env}
      [ -f ./ci/${this.jobname}.sh ] || { echo "noop"; exit; }
      chmod 700 ./ci/${this.jobname}.sh
      ./ci/${this.jobname}.sh
      exit \$?
    """
  }

  def _archive_script() {
    return """
      ${this._app_env}

      mv \$root/\$APP.\$EXT \$artifact

      # TODO: use Venice instead of Piazza
      mvn --quiet dependency:get \
        -DremoteRepositories="nexus::default::https://nexus.devops.geointservices.io/content/repositories/Piazza" \
        -DrepositoryId=nexus \
        -DartifactId=\$APP \
        -DgroupId=org.${this.config.nexus_org}.${this.config.team}\
        -Dpackaging=\$EXT \
        -Dtransitive=false \
        -Dversion=\$version \
      && { echo "artifact already exists! Noop!"; exit 0; } || true

      # pom?
      [ -f \$root/pom.xml ] && genpom=false || genpom=false

      # push artifact to nexus
      mvn --quiet deploy:deploy-file \
        -Durl="https://nexus.devops.geointservices.io/content/repositories/Piazza" \
        -DrepositoryId=nexus \
        -Dfile=\$artifact \
        -DgeneratePom=\$genpom \
        -DgroupId=org.${this.config.nexus_org}.${this.config.team}\
        -DartifactId=\$APP \
        -Dversion=\$version \
        -Dpackaging=\$EXT

      rm -f \$artifact
    """
  }

  def _cf_push_script() {
    return """
      ${this._app_env}

      mvn --quiet dependency:get \
        -DremoteRepositories="nexus::default::https://nexus.devops.geointservices.io/content/repositories/Piazza" \
        -DrepositoryId=nexus \
        -DartifactId=\$APP \
        -DgroupId=org.${this.config.nexus_org}.${this.config.team}\
        -Dpackaging=\$EXT \
        -Dtransitive=false \
        -Dversion=\$version \
        -Ddest=\$root/\$APP.\$EXT

      [ "bin" = "\$EXT" ] && chmod 755 \$root/\$APP.\$EXT
      [ "tar.gz" = "\$EXT" ] && tar -xzf \$root/\$APP.\$EXT
      [ "tgz" = "\$EXT" ] && tar -xzf \$root/\$APP.\$EXT

      [ -f \$root/\$APP.\$EXT ] || exit 1

      ${this._pcf_env}
      ${this._cf_auth}

      set +e

      [ -f manifest.\$PCF_SPACE.yml ] && manifest=manifest.\$PCF_SPACE.yml || manifest=manifest.jenkins.yml

      if ! grep -q DOMAIN \$manifest; then
        grep -q env \$manifest && echo "    DOMAIN: \$PCF_DOMAIN\n    SPACE: \$PCF_SPACE" >> \$manifest || echo "  env: {DOMAIN: \$PCF_DOMAIN, SPACE: \$PCF_SPACE}" >> \$manifest
      fi

      if [ -n "\$JKS" ]; then
        mv \$JKS \$root/pz.jks
        echo "    JKS_FILE: /home/vcap/app/pz.jks" >> \$manifest
        echo "    JKS_PASSPHRASE: \$JKS_PASSPHRASE" >> \$manifest
        echo "    PZ_PASSPHRASE: \$PZ_PASSPHRASE" >> \$manifest
      fi

      cf app \$APP-\$version && { echo " \$APP-\$version already running."; exit 0; } || echo "Pushing \$APP-\$version."

      cf push \$APP-\$version -f \$manifest --hostname \$cfhostname -d \$PCF_DOMAIN

      if [ \$? != 0 ]; then
        echo "Printing log output as a result of the failure."
        cf logs --recent \$APP-\$version
        cf delete \$APP-\$version -f -r
        rm -f \$root/\$APP.\$EXT
        exit 1
      fi

      rm -f \$root/\$APP.\$EXT
    """
  }

  def _cf_push_release_script() {
    return """
      ${this._app_env}
      ${this._pcf_env}
      ${this._cf_auth}

      set +e

      [ -f manifest.\$PCF_SPACE.yml ] && manifest=manifest.\$PCF_SPACE.yml || manifest=manifest.jenkins.yml

      if ! grep -q DOMAIN \$manifest; then
        grep -q env \$manifest && echo "    DOMAIN: \$PCF_DOMAIN\n    SPACE: \$PCF_SPACE" >> \$manifest || echo "  env: {DOMAIN: \$PCF_DOMAIN, SPACE: \$PCF_SPACE}" >> \$manifest
      fi

      cf app \$APP-\$version && { echo " \$APP-\$version already running."; exit 0; } || echo "Pushing \$APP-\$version."

      cf push \$APP-\$version -f \$manifest --hostname \$cfhostname -d \$PCF_DOMAIN

      if [ \$? != 0 ]; then
        echo "Printing log output as a result of the failure."
        cf logs --recent \$APP-\$version
        cf delete \$APP-\$version -f -r
        rm -f \$root/\$APP.\$EXT
        exit 1
      fi
    """
  }

  def _cf_bg_deploy_script() {
    return """
      ${this._app_env}
      ${this._pcf_env}
      ${this._cf_auth}

      set +e

      legacy=`cf routes | grep "\$APP " | awk '{print \$4}'`
      target=\$APP-\$version
      cf app \$target || exit 1
      [ "\$target" = "\$legacy" ] && { echo "nothing to do."; exit 0; }
      cf map-route \$APP-\$version \$PCF_DOMAIN --hostname \$APP
      s=\$?
      [ "\$APP" = "pz-gateway" ] && cf map-route \$APP-\$version \$PCF_DOMAIN --hostname piazza
      [ -n "\$legacy" ] && cf unmap-route "\$legacy" \$PCF_DOMAIN --hostname \$APP
      [ -n "\$legacy" ] && [ "\$APP" = "pz-gateway" ] && cf unmap-route "\$legacy" \$PCF_DOMAIN --hostname piazza
      [ -n "\$legacy" ] || exit \$s
      IFS=,
      for route in "\$legacy" ; do
        [ "\$target" = "\$route" ] && continue
        cf unmap-route "\$route" \$PCF_DOMAIN --hostname \$APP
        cf delete "\$route" -f -r
      done
    """
  }

  def _cf_set_version_script() {
    return """
      ${this._app_env}
      ${this._pcf_env}
      ${this._cf_auth}

      x=\$(cf apps | grep \$APP | awk '{print \$1}' | awk -F '-' '{print \$NF}')

      version=\${x: -7}

      git checkout \$version

      component="${this.config.gh_repo}"
      component_revision=\$(git rev-parse HEAD)
    """
  }

  def _create_properties_file_script() {
    return """
      rm -f pipeline.properties
      echo "component=${this.config.gh_repo}" >> pipeline.properties
      echo "component_revision=\$GIT_COMMIT" >> pipeline.properties
    """
  }

  def _pass_properties_file_script() {
    return """
      rm -f pipeline.properties
      echo "component=\$component" >> pipeline.properties
      echo "component_revision=\$component_revision" >> pipeline.properties
    """
  }

  def _github_write_script() {
    return """
      [ -f "\$GIT_KEY" ] && ssh-add "\$GIT_KEY"
      chmod 600 \$HOME/.ssh/config
      cat <<- EOF > \$HOME/.ssh/config
Host github.com-venice
  HostName github.com
  User git
  IdentityFile \$GIT_KEY
  IdentitiesOnly yes
EOF
      chmod 400 \$HOME/.ssh/config

      git remote set-url origin git@github.com-venice:venicegeo/pz-release.git
    """
  }

  def _ionchannel_pom_script() {
    return """
      root=\$(pwd -P)

      set +x
      export HISTFILE=/def/null
      [ -z "\$IONCHANNEL_SECRET_KEY" ] && { echo "IONCHANNEL_SECRET_KEY not set" >&2; exit 1; }
      [ -z "\$IONCHANNEL_ENDPOINT_URL" ] && IONCHANNEL_ENDPOINT_URL=https://api.private.ionchannel.io

      os=\$(uname -s | tr '[:upper:]' '[:lower:]')
      srcpom=\$root/pom.xml
      pomfile=\$root/tmp/pom.xml
      archive=ion-connect-latest.tar.gz

      mkdir -p \$root/tmp/bin

      # Install jq?
      if ! type jq >/dev/null 2>&1; then

        if [ "\$os" = "linux" ]; then
          uname -m | grep -q 64 \
            && curl -o \$root/tmp/bin/jq -O https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux64 \
            || curl -o \$root/tmp/bin/jq -O https://github.com/stedolan/jq/releases/download/jq-1.5/jq-linux32
        elif [ "\$os" = "darwin" ]; then
          curl -o \$root/tmp/bin/jq -O https://github.com/stedolan/jq/releases/download/jq-1.5/jq-osx-amd64
        else
          echo "jq install on \$os not supported by this script; please visit https://stedolan.github.io/jq/download/" >&2
          exit 1
        fi

        chmod 700 \$root/tmp/bin/jq
        jqcmd=\$root/tmp/bin/jq
      else
        jqcmd=jq
      fi

      # Remove private repos from the pomfile
      cat \$srcpom | perl -000 -ne 'print unless /org.venice.piazza/ && /pz-jobcommon/ && /dependency/' > \$pomfile

      # Install ion-connect?
      curl -o \$root/tmp/\$archive -O https://s3.amazonaws.com/public.ionchannel.io/files/ion-connect/\$archive
      tar -C \$root/tmp -xzf \$root/tmp/\$archive
      ioncmd=\$root/tmp/ion-connect/\$os/bin/ion-connect

      \$ioncmd --version

      echo && echo "ION OUTPUT:" && echo
      \$ioncmd vulnerability get-vulnerabilities-for-list \
        \$(\$ioncmd dependency resolve-dependencies-in-file --flatten --type maven \$pomfile \
            | \$jqcmd -c .dependencies)
      echo

      ion_status=\$?

      rm -rf \$root/tmp

      exit 0
    """
  }
}
