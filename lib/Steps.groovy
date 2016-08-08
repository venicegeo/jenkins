package lib

class Steps {
  def jobject
  def config
  def jobname
  def override = ""

  def init() {
    this._pcf_env="""
      override="${this.override}"
      [ -z "\$override" ] && target_domain=\$override
      case \$target_domain in
        int.geointservices.io)    export PCF_SPACE=${this.config.envs.int.space}   ; export PCF_DOMAIN=${this.config.envs.int.domain}   ; export PCF_API=${this.config.envs.int.api}   ; export PCF_ORG=${this.config.pcf_org} ;;
        stage.geointservices.io)  export PCF_SPACE=${this.config.envs.stage.space} ; export PCF_DOMAIN=${this.config.envs.stage.domain} ; export PCF_API=${this.config.envs.stage.api} ; export PCF_ORG=${this.config.pcf_org} ;;
        dev.geointservices.io)    export PCF_SPACE=${this.config.envs.dev.space}   ; export PCF_DOMAIN=${this.config.envs.dev.domain}   ; export PCF_API=${this.config.envs.dev.api}   ; export PCF_ORG=${this.config.pcf_org} ;;
        test.geointservices.io)   export PCF_SPACE=${this.config.envs.test.space}  ; export PCF_DOMAIN=${this.config.envs.test.domain}  ; export PCF_API=${this.config.envs.test.api}  ; export PCF_ORG=${this.config.pcf_org} ;;
        geointservices.io)        export PCF_SPACE=${this.config.envs.prod.space}  ; export PCF_DOMAIN=${this.config.envs.prod.domain}  ; export PCF_API=${this.config.envs.prod.api}  ; export PCF_ORG=${this.config.pcf_org} ;;
      esac
    """

    return this
  }

  def defaults() {
    this.jobject.with {
      steps {
        shell(this._git_script())
        shell(this._job_script())
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

  def cf_push() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('CF_USER', 'CF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
        }
      }
      steps {
        shell(this._cf_push_script())
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

  def cf_bg_deploy() {
    this.jobject.with {
      wrappers {
        credentialsBinding {
          usernamePassword('CF_USER', 'CF_PASSWORD', '6ad30d14-e498-11e5-9730-9a79f06e9478')
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
        shell('npm install -g newman@2')
      }
    }

    return this
  }

  private String _pcf_env

  private String _app_env="""
    root=\$(pwd -P)

    [ ! -f \$root/ci/vars.sh ] && echo "No vars.sh" && exit 1
    source \$root/ci/vars.sh

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
      [ "\$revision" != "latest" ] && git checkout \$revision || echo "using latest revision"
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

      mvn --quiet dependency:get \
        -DremoteRepositories="nexus::default::https://nexus.devops.geointservices.io/content/repositories/${this.config.team.capitalize()}" \
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
        -Durl="https://nexus.devops.geointservices.io/content/repositories/${this.config.team.capitalize()}" \
        -DrepositoryId=nexus \
        -Dfile=\$artifact \
        -DgeneratePom=\$genpom \
        -DgroupId=org.${this.config.nexus_org}.${this.config.team}\
        -DartifactId=\$APP \
        -Dversion=\$version \
        -Dpackaging=\$EXT

      rm \$artifact
    """
  }

  def _cf_push_script() {
    return """
      ${this._app_env}

      mvn --quiet dependency:get \
        -DremoteRepositories="nexus::default::https://nexus.devops.geointservices.io/content/repositories/${this.config.team.capitalize()}" \
        -DrepositoryId=nexus \
        -DartifactId=\$APP \
        -DgroupId=org.${this.config.nexus_org}.${this.config.team}\
        -Dpackaging=\$EXT \
        -Dtransitive=false \
        -Dversion=\$version \
        -Ddest=\$root/\$APP.\$EXT

      [ "bin" = "\$EXT" ] && chmod 755 \$root/\$APP.\$EXT
      [ "tar.gz" = "\$EXT" ] && tar -xzf \$root/\$APP.\$EXT

      [ -f \$root/\$APP.\$EXT ] || exit 1

      ${this._pcf_env}
      ${this._cf_auth}

      set +e

      [ -f manifest.\$PCF_SPACE.yml ] && manifest=manifest.\$PCF_SPACE.yml || manifest=manifest.jenkins.yml

      grep -q env \$manifest && echo "    DOMAIN: \$PCF_DOMAIN\n    SPACE: \$PCF_SPACE" >> \$manifest || echo "  env: {DOMAIN: \$PCF_DOMAIN, SPACE: \$PCF_SPACE}" >> \$manifest

      cf push \$APP-\$version -f \$manifest --hostname \$cfhostname -d \$PCF_DOMAIN

      if [ \$? != 0 ]; then
        cf logs --recent \$APP-\$version
        cf delete \$APP-\$version -f -r
        rm \$root/\$APP.\$EXT
        exit 1
      fi

      rm \$root/\$APP.\$EXT
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
      [ -n "\$legacy" ] && cf unmap-route "\$legacy" \$PCF_DOMAIN --hostname \$APP
      [ -n "\$legacy" ] || exit \$s
      IFS=,
      for route in "\$legacy" ; do
        [ "\$target" = "\$route" ] && continue
        cf unmap-route "\$route" \$PCF_DOMAIN --hostname \$APP
        cf delete "\$route" -f -r
      done
    """
  }
}
