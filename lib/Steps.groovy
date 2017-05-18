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
          if (this.config.gh_repo == 'pz-idam') {
            string('JKS_PASSPHRASE', 'ff7148c6-2855-4f3d-bd2e-3aa296b09d98')
            string('PZ_PASSPHRASE', 'da3092c4-d13d-4078-ab91-a630c61547aa')
          }
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
          if (this.config.gh_repo == 'pz-idam') {
            file('JKS', 'ca8591a7-fc1f-4b6d-808e-c9944c9bf4f8')
          }
          if (this.config.gh_repo == 'bf-ia-broker') {
            string('PL_API_KEY', 'e5b7076b-885a-43ba-9626-30ff950bd790')
          }
          if (this.config.gh_repo == 'bf-ui') {
            string('CONSENT_BANNER_TEXT', '824eee31-0408-49e2-9a7e-70b59297b1b9')
          }
        }

        customTools(['Maven3_custom_tool']) {
          skipMasterInstallation true
          convertHomesToUppercase true
        }
      }
      if (this.config.gh_repo == 'bf-ui') {
       configure { project ->
          project / buildWrappers << 'jenkins.plugins.nodejs.tools.NpmPackagesBuildWrapper' {
            nodeJSInstallationName "Node_7"
          }
        }
      }
      steps {
        shell(this._archive_script())
      }
    }

    return this
  }

  def gitlab_push() {
    this.jobject.with {
      steps {
        shell(this._gitlab_push_script())
      }
    }

    return this
  }

  def zap() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          string('THREADFIX_KEY', '978C467A-2B26-47AE-AD2F-4AFD5A4AF695')
        }
        customTools(['ZAProxy']) {
          skipMasterInstallation true
          convertHomesToUppercase true
        }
      }
      steps {
        shell(this._zap_script())
      }
    }

    return this
  }

  def ionchannel_pom() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          string('IONCHANNEL_SECRET_KEY', 'fbbfbbd2-7e31-46ac-b3ac-b66c4f2ae2e4')
        }
        customTools(['jq1_5', 'ion_connect_latest']) {
          skipMasterInstallation true
          convertHomesToUppercase true
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
      steps {
        shell """
          src=\$(find src/main -name *.java)
          [ ! -f \$src ] && echo "Source not found." && exit 1
          /jslave/tools/hudson.tasks.Maven_MavenInstallation/M3/bin/mvn install:install-file -Dfile=pom.xml -DpomFile=pom.xml 
          /opt/hp_fortify_sca/bin/sourceanalyzer -b \${BUILD_NUMBER} \$src
          /opt/hp_fortify_sca/bin/sourceanalyzer -b \${BUILD_NUMBER}  -scan -Xmx1G -f fortifyResults-\${BUILD_NUMBER}.fpr
          /bin/curl -v --insecure -H 'Accept: application/json' -X POST --form file=@fortifyResults-\${BUILD_NUMBER}.fpr https://threadfix.devops.geointservices.io/rest/applications/${this.config.threadfix_id}/upload?apiKey=\${THREADFIX_KEY}
          #/opt/hp_fortify_sca/bin/ReportGenerator -format pdf -f ${this.config.gh_repo}-fortify-\${BUILD_NUMBER}.pdf -source fortifyResults-\${BUILD_NUMBER}.fpr"
        """
      }
    }
  }

  def sonar() {
    this.jobject.with {
      jdk "JDK 1.8uLATEST"

      wrappers {
        credentialsBinding {
          string('REDMINE_KEY', 'C0C13D9C-C21F-4DDE-9AC9-6965E31E54B7')
        }
        customTools(['Maven3_custom_tool']) {
          skipMasterInstallation true
          convertHomesToUppercase true
        }
      }

      configure { project ->
        project / buildWrappers << 'jenkins.plugins.nodejs.tools.NpmPackagesBuildWrapper' {
          nodeJSInstallationName "Node_7"
        }
      }

      environmentVariables {
        env('APP', "${this.config.gh_repo}")
        env('GH_ORG', "${this.config.gh_org}")
        env('JENKINS_ORG', "${this.config.jenkins_org}")
        env('TEAM', "${this.config.team}")
      }

      steps {
        shell """
          for bin in \$(find /jslave/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/Maven3_custom_tool -name bin); do
            export PATH=\$PATH:\$bin
          done
        """

        sonarRunnerBuilder {
          installationName "DevOps Sonar"
          sonarScannerName "SonarQube Runner 2.8"
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
          if (this.config.gh_repo == 'bf-api' || this.config.gh_repo == 'bftest-integration') {
            usernamePassword('BEACHFRONT_PIAZZA_AUTH', '93a0311a-caac-4f5a-bfcb-9ad18b0c0cd1')
            string('BEACHFRONT_GEOAXIS_CLIENT_ID', 'b81d7d20-3576-4f02-ac90-4e6fd5a9d453')
            string('BEACHFRONT_GEOAXIS_SECRET', 'e83dfc65-4462-4a80-a04d-57ab8da20ebd')
          }
          if (this.config.gh_repo == 'pzsvc-ossim' || this.config.gh_repo == 'pzsvc-ndwi-py') {
            usernamePassword('BEACHFRONT_PIAZZA_AUTH', '93a0311a-caac-4f5a-bfcb-9ad18b0c0cd1')
          }
          if (this.config.gh_repo == 'pz-idam') {
            string('JKS_PASSPHRASE', 'ff7148c6-2855-4f3d-bd2e-3aa296b09d98')
            string('PZ_PASSPHRASE', 'da3092c4-d13d-4078-ab91-a630c61547aa')
          }
        }
        customTools(['Maven3_custom_tool']) {
          skipMasterInstallation true
          convertHomesToUppercase true
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
          if (this.config.gh_repo == 'pz-idam') {
            string('JKS_PASSPHRASE', 'ff7148c6-2855-4f3d-bd2e-3aa296b09d98')
            string('PZ_PASSPHRASE', 'da3092c4-d13d-4078-ab91-a630c61547aa')
          }
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
          if (this.config.gh_repo == 'pz-idam') {
            string('JKS_PASSPHRASE', 'ff7148c6-2855-4f3d-bd2e-3aa296b09d98')
            string('PZ_PASSPHRASE', 'da3092c4-d13d-4078-ab91-a630c61547aa')
          }
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

  def cf_release_dev() { 
        this.override = "dev.geointservices.io" 
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
    this.override = "int.geointservices.io"
    this.init()
    this.cf_set_version()

    this.override = "test.geointservices.io"
    this.init()
    this.cf_push()
    this.cf_bg_deploy()

    return this
  }

  def cf_promote_to_dev() {
    this.override = "int.geointservices.io"
    this.init()
    this.cf_set_version()

    this.override = "dev.geointservices.io"
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
          if (this.config.gh_repo == 'pz-idam') {
            string('JKS_PASSPHRASE', 'ff7148c6-2855-4f3d-bd2e-3aa296b09d98')
            string('PZ_PASSPHRASE', 'da3092c4-d13d-4078-ab91-a630c61547aa')
          }
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
      wrappers {
        credentialsBinding {
          file('POSTMAN_FILE', '579f8660-01e6-4feb-8764-ec132432ebb1')
          if (this.config.team == 'beachfront') {
            string('bf_username', 'e3799eb1-95df-4285-a24e-6721cd690daa')
            string('bf_password', '40ce94f3-3c14-40d6-a75b-b48556a0c560')
          }
        }
      }
      configure { project ->
        project / buildWrappers << 'jenkins.plugins.nodejs.tools.NpmPackagesBuildWrapper' {
          nodeJSInstallationName "Node_7"
        }
      }

      steps {
        shell(this._blackbox_script())
      }
    }

    return this
  }
  def bf_test_secrets() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          string('PL_API_KEY',  '7a64953f-283a-4a28-824f-4e96760574e8')
          string('bf_username', 'e3799eb1-95df-4285-a24e-6721cd690daa')
          string('bf_password', '40ce94f3-3c14-40d6-a75b-b48556a0c560')
          usernamePassword('sauce_user', 'sauce_key', '1ba84f72-0a02-45e2-8869-cfa62df01251')
          file('POSTMAN_FILE', '579f8660-01e6-4feb-8764-ec132432ebb1')
        }
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

      for bin in \$(find /jslave/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/Maven3_custom_tool -name bin); do
        export PATH=\$PATH:\$bin
      done
      export PATH=\$PATH:\$HOME/bin

      [ -f ./ci/${this.jobname}.sh ] || { echo "noop"; exit; }
      chmod 700 ./ci/${this.jobname}.sh
      ./ci/${this.jobname}.sh
      exit \$?
    """
  }

  def _archive_script() {
    return """
      ${this._app_env}

      for bin in \$(find /jslave/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/Maven3_custom_tool -name bin); do
        export PATH=\$PATH:\$bin
      done

      root=\$(pwd -P)
      mv \$root/\$APP.\$EXT \$artifact

      mkdir -p \$root/.m2/repository

      # TODO: use Venice instead of Piazza
      mvn --quiet dependency:get \
        -Dmaven.repo.local="\$root/.m2/repository" \
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
        -Dmaven.repo.local="\$root/.m2/repository" \
        -Durl="https://nexus.devops.geointservices.io/content/repositories/Piazza" \
        -DrepositoryId=nexus \
        -Dfile=\$artifact \
        -DgeneratePom=\$genpom \
        -DgroupId=org.${this.config.nexus_org}.${this.config.team}\
        -DartifactId=\$APP \
        -Dversion=\$version \
        -Dpackaging=\$EXT

      push_status=\$?

      [ ! -f \$artifact ] || rm -f \$artifact

      exit \$push_status
    """
  }

  def _cf_push_script() {
    return """
      ${this._app_env}

      for bin in \$(find /jslave/tools/com.cloudbees.jenkins.plugins.customtools.CustomTool/Maven3_custom_tool -name bin); do
        export PATH=\$PATH:\$bin
      done

      root=\$(pwd -P)
      mkdir -p \$root/.m2/repository

      mvn --quiet dependency:get \
        -Dmaven.repo.local="\$root/.m2/repository" \
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

      if [ -n "\$JKS_PASSPHRASE" ]; then
        echo "    JKS_FILE: pz-idam.jks" >> \$manifest
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

      [ ! -f \$root/\$APP.\$EXT ] || rm -f \$root/\$APP.\$EXT
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
      eval "\$(ssh-agent -s)"
      if [ ! -f \$HOME/.ssh/config ]; then
            touch \$HOME/.ssh/config
      fi
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

      # jq required for json processing
      if type jq >/dev/null 2>&1; then
        jqcmd=jq
      else
        test -n "\$JQ1_5_HOME" || { echo "JQ1_5_HOME not set" >&2; exit 1; }
        jqcmd=\$JQ1_5_HOME/jq
        test -x \$jqcmd || { echo "\$jqcmd not available." >&2; exit 1; }
      fi

      # ion-connect is required
      if type ion-connect >/dev/null 2>&1; then
        ioncmd=ion-connect
      else
        test -n "\$ION_CONNECT_LATEST_HOME" || { echo "ION_CONNECT_LATEST_HOME not set" >&2; exit 1; }
        os=\$(uname -s | tr '[:upper:]' '[:lower:]')
        ioncmd=\$(echo \$ION_CONNECT_LATEST_HOME | sed 's/-latest\$//')/\$os/bin/ion-connect
        test -x \$ioncmd || { echo "\$ioncmd not available." >&2; exit 1; }
      fi

      \$ioncmd --version

      do_xtrace=\$(echo \$SHELLOPTS | grep -o xtrace | cat)
      set +x
      oldhistfile="\$HISTFILE"
      export HISTFILE=/dev/null

      test -n "\$IONCHANNEL_SECRET_KEY"   || { echo "IONCHANNEL_SECRET_KEY not set" >&2; exit 1; }
      test -n "\$IONCHANNEL_ENDPOINT_URL" || IONCHANNEL_ENDPOINT_URL=https://api.private.ionchannel.io

      export HISTFILE="\$oldhistfile"
      [ -z "\$do_xtrace" ] || set -x


      for pomfile in \$(find \$root -name pom.xml); do
        echo && echo "ION OUTPUT:" && echo
        deps=\$(\$ioncmd dependency resolve-dependencies-in-file --flatten --type maven \$pomfile | \$jqcmd .dependencies) && \$ioncmd --debug  vulnerability get-vulnerabilities-for-list "\${deps}"
        echo
      done
    """
  }

  def _gitlab_push_script() {
    return """
      root=\$(pwd -P)

      git push git@gitlab.devops.geointservices.io:${this.config.gh_org}/${this.config.gh_repo} master

      exit \$?
    """
  }
  
  def _blackbox_script() {
    return """
      HOME=\$WORKSPACE
      export root=\$(pwd)
      mkdir -p \$root/.npmcache
      export NPM_CONFIG_CACHE=\$root/.npmcache
      npm set cache \$(pwd)/.npmcache; npm install newman@2 karma-cli
    """
  }

  def _zap_script() {
    return """
      root=\$(pwd -P)
      ${this._app_env}
      ${this._pcf_env}

      set +e

      mkdir -p \$root/out
      chmod 777 \$root/out
      \$ZAPROXY_HOME/zap.sh -cmd \
        -quickout \$root/out/\$cfhostname.xml \
        -quickurl https://\$cfhostname.\$PCF_DOMAIN

      cat \$root/out/\$cfhostname.xml

      curl --silent  --fail  --trace-ascii "threadfix-zap-upload-curl-trace.log" \
        -H 'Accept: application/json'  -X POST  --form file=@\$root/out/\$cfhostname.xml \
        https://threadfix.devops.geointservices.io/rest/applications/${this.config.threadfix_id}/upload?apiKey=\${THREADFIX_KEY}

      exit_status=\$?

      rm -rf \$root/out

      exit 0
    """
  }
}
