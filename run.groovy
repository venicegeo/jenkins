import lib.Base
import lib.Steps
import static Repos.repos

def global_config = [
  envs: [
    int:    [space: 'int',             domain: 'int.geointservices.io',   api: 'https://api.devops.geointservices.io'],
    stage:  [space: 'stage',           domain: 'stage.geointservices.io', api: 'https://api.devops.geointservices.io'],
    dev:    [space: 'dev',             domain: 'dev.geointservices.io',   api: 'https://api.devops.geointservices.io'],
    test:   [space: 'test',            domain: 'test.geointservices.io',  api: 'https://api.devops.geointservices.io'],
    prod:   [space: 'prod',            domain: 'geointservices.io',       api: 'https://api.devops.geointservices.io']
  ]
]

def entries = [:]
// rearrange the job list for processing.
for (p in repos) {
  entries[p.reponame] = [
    team: p.team,
    gh_org: p.gh_org,
    branch: p.branch,
    lib: p.lib || false,
    manual: p.manual,
    jobs: [:]
  ]

  p.pipeline.eachWithIndex { jobname, idx ->

    entries[p.reponame].jobs[jobname] = [
      index: idx,
      children: [:]
    ]

    if (p.pipeline[idx+1]) {
      entries[p.reponame].jobs[jobname].children[idx+1] = p.pipeline[idx+1]
    }
  }
}

// repo loop
entries.each{ reponame, entry ->
  def config = [
    gh_repo: reponame,
    gh_org: entry.gh_org ? entry.gh_org : 'venicegeo',
    gh_branch: entry.branch ? entry.branch : 'master',
    team: entry.team ? entry.team : 'piazza',
    slack_token: binding.variables.get("SLACK_TOKEN"),
    slack_domain: "venicegeo",
    pcf_org: "piazza",
    jenkins_org: "venice",
    nexus_org: "venice",
    envs: global_config.envs,
    domains: ['int.geointservices.io', 'stage.geointservices.io', 'dev.geointservices.io', 'test.geointservices.io', 'geointservices.io'],
    domains_description: 'PCF Domain/Space to target<br>&nbsp;&nbsp;<b>geointservices.io</b>: production<br>&nbsp;&nbsp;<b>stage.geointservices.io</b>: beta<br>&nbsp;&nbsp;<b>int.geointservices.io</b>: CI<br>&nbsp;&nbsp;<b>dev.geointservices.io</b>: developer sandbox<br>&nbsp;&nbsp;<b>test.geointservices.io</b>: test bed'
  ]

  folder("${config.jenkins_org}/${config.team}") {
    displayName("${config.team}")
  }

  folder("${config.jenkins_org}/${config.team}/${config.gh_repo}") {
    displayName("${config.gh_repo}")
  }

  // job loop
  entry.jobs.each{ jobname, data ->
    def mutant = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/${data.index}-${jobname}")
    def base_job

    def steps = new Steps(
      jobject: mutant,
      config: config,
      jobname: jobname
    ).init()

    if (jobname.contains("integration_tests")) {
      base_job = new Base(
        jobject: mutant,
        config: [
          gh_org: 'venicegeo',
          gh_repo: 'pztest-integration',
          gh_branch: 'master',
          slack_token: binding.variables.get("SLACK_TOKEN"),
          slack_domain: "venicegeo"
        ]
      ).defaults().github()

      steps.blackbox()

    } else if (jobname.contains("ua_tests")) {
      base_job = new Base(
        jobject: mutant,
        config: [
          gh_org: 'venicegeo',
          gh_repo: 'bftest-ui',
          gh_branch: 'master',
          slack_token: binding.variables.get("SLACK_TOKEN"),
          slack_domain: "venicegeo"
        ]
      ).defaults().github().selenium()

    } else if (jobname.contains("release")) {
      def release_branch
      switch (jobname) {
        case 'ci-release':
          release_branch = 'ci'
          break
        case 'test-release':
          release_branch = 'test'
          break
        case 'stage-release':
          release_branch = 'rc'
          break
        case 'prod-release':
          release_branch = 'master'
          break
        default:
          release_branch = 'ci'
      }

      base_job = new Base(
        jobject: mutant,
        slack_message: "      component: `\$component`\n      component_revision: `\$component_revision`",
        config: [
          gh_org: 'venicegeo',
          gh_repo: 'pz-release',
          gh_branch: release_branch,
          slack_token: binding.variables.get("SLACK_TOKEN"),
          slack_domain: "venicegeo"
        ]
      ).defaults().github()

      steps.gh_write()

    } else {
      // Do not checkout integration tests
      steps.git_checkout()

      if (jobname == "karma") {
        steps.blackbox()
      }

      base_job = new Base(
        jobject: mutant,
        promotion: (jobname == 'cf_bg_deploy_stage'),
        slack_message: "      commit sha: `\$GIT_COMMIT`",
        config: config
      ).defaults().github()
    }

    steps.job_script()

    if (steps.metaClass.respondsTo(steps, jobname)) {
      steps."${jobname}"()
    }

    if (jobname.contains('build')) {
      steps.archive()
    }

    if (jobname == "stage-release") {
      steps.cf_release_stage()
    }

    if (jobname == "int-release") {
      steps.cf_release_int()
    }

    if (jobname.contains("selenium")) {
      base_job.selenium()
    }

    if (jobname.contains("bf_tideprediction")) {
      base_job.sl55()
    }

    if (data.index == 0) {
      // first job in pipeline needs an external trigger.
      steps.gh_trigger()

      // and our properties file
      steps.create_properties_file()

      // construct the pipeline view
      buildPipelineView("${config.jenkins_org}/${config.team}/${config.gh_repo}/pipeline") {
        filterBuildQueue()
        filterExecutors()
        title("${config.gh_repo} build pipeline")
        displayedBuilds(5)
        selectedJob("${config.jenkins_org}/${config.team}/${config.gh_repo}/${data.index}-${jobname}")
        alwaysAllowManualTrigger()
        showPipelineParameters()
        refreshFrequency(60)
      }
    } else {
      // and our properties file
      steps.pass_properties_file()
      base_job.pipeline_parameters()
    }

    // define downstream jobs
    if (data.children) {
      data.children.each { idx, childname ->
        mutant.with {
          publishers {
            downstreamParameterized {
              trigger("${config.jenkins_org}/${config.team}/${config.gh_repo}/${idx}-${childname}") {
                condition('SUCCESS')
                parameters {
                  propertiesFile('pipeline.properties', true)
                }
              }
            }
          }
        }
      }
    }

  }

  def promotion_job
  def promotion_base
  def promotion_steps
  def release_job
  def release_base
  def release_steps
  def test_promotion_job
  def test_promotion_base
  def test_promotion_steps
  def test_release_job
  def test_release_base
  def test_release_steps
  if (entry.team == 'piazza' && entry.lib != true) {
    // -- production pipeline
    folder("${config.jenkins_org}/${config.team}/${config.gh_repo}/production") {
      displayName("${config.gh_repo}/production")
    }

    promotion_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/production/0-promote")
    promotion_base = new Base(
      jobject: promotion_job,
      promotion: true,
      slack_message: "      component_revision: `\$component_revision`\n      domain: `\$target_domain`\n      commit sha: `\$GIT_COMMIT`",
      config: config
    ).defaults().github().parameters()

    promotion_steps = new Steps(
      jobject: promotion_job,
      config: config,
      jobname: "promote"
    ).init().git_checkout().job_script().cf_promote_to_prod().create_properties_file()

    release_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/production/1-release")

    release_base = new Base(
      jobject: release_job,
      slack_message: "      component: `\$component`\n      component_revision: `\$component_revision`",
      config: [
        gh_org: 'venicegeo',
        gh_repo: 'pz-release',
        gh_branch: 'master',
        slack_token: binding.variables.get("SLACK_TOKEN"),
        slack_domain: "venicegeo"
      ]
    ).defaults().github()

    release_steps = new Steps(
      jobject: release_job,
      config: config,
      jobname: 'prod-release'
    ).init().gh_write().job_script().cf_release_prod()

    promotion_job.with {
      publishers {
        downstreamParameterized {
          trigger("${config.jenkins_org}/${config.team}/${config.gh_repo}/production/1-release") {
            condition('SUCCESS')
            parameters {
              propertiesFile('pipeline.properties', true)
            }
          }
        }
      }
    }
    // -- end production

    // -- hotfix pipeline
    folder("${config.jenkins_org}/${config.team}/${config.gh_repo}/hotfix-prod") {
      displayName("${config.gh_repo}/hotfix-prod")
    }
    hotfix_archive_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/hotfix-prod/0-archive")
    hotfix_archive_base = new Base(
      jobject: hotfix_archive_job,
      promotion: false,
      slack_message: "      component_revision: `\$component_revision`\n      domain: `\$target_domain`\n      commit sha: `\$GIT_COMMIT`",
      config: config
    ).defaults().github().parameters()

    hotfix_archive_steps = new Steps(
      jobject: hotfix_archive_job,
      config: config,
      jobname: "archive"
    ).init().git_checkout().job_script().archive().create_properties_file()


    hotfix_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/hotfix-prod/1-deploy")
    hotfix_base = new Base(
      jobject: hotfix_job,
      promotion: false,
      slack_message: "      component_revision: `\$component_revision`\n      domain: `\$target_domain`\n      commit sha: `\$GIT_COMMIT`",
      config: config
    ).defaults().github().parameters()

    hotfix_steps = new Steps(
      jobject: hotfix_job,
      config: config,
      jobname: "hotfix"
    ).init().git_checkout().job_script().cf_hotfix_prod().create_properties_file()

    hotfix_release_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/hotfix-prod/2-release")

    hotfix_release_base = new Base(
      jobject: hotfix_release_job,
      slack_message: "      component: `\$component`\n      component_revision: `\$component_revision`",
      config: [
        gh_org: 'venicegeo',
        gh_repo: 'pz-release',
        gh_branch: 'master',
        slack_token: binding.variables.get("SLACK_TOKEN"),
        slack_domain: "venicegeo"
      ]
    ).defaults().github()

    hotfix_release_steps = new Steps(
      jobject: release_job,
      config: config,
      jobname: 'prod-release'
    ).init().gh_write().job_script().cf_release_prod()

    hotfix_archive_job.with {
      publishers {
        downstreamParameterized {
          trigger("${config.jenkins_org}/${config.team}/${config.gh_repo}/hotfix-prod/1-deploy") {
            condition('SUCCESS')
            parameters {
              propertiesFile('pipeline.properties', true)
            }
          }
        }
      }
    }

    hotfix_job.with {
      publishers {
        downstreamParameterized {
          trigger("${config.jenkins_org}/${config.team}/${config.gh_repo}/hotfix-prod/2-release") {
            condition('SUCCESS')
            parameters {
              propertiesFile('pipeline.properties', true)
            }
          }
        }
      }
    }
    // -- end production

    // -- load test pipeline
    folder("${config.jenkins_org}/${config.team}/${config.gh_repo}/test") {
      displayName("${config.gh_repo}/test")
    }

    test_promotion_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/test/0-promote")
    test_promotion_base = new Base(
      jobject: test_promotion_job,
      slack_message: "      component_revision: `\$component_revision`\n      domain: `\$target_domain`\n      commit sha: `\$GIT_COMMIT`",
      config: config
    ).defaults().github()

    test_promotion_steps = new Steps(
      jobject: test_promotion_job,
      config: config,
      jobname: "promote"
    ).init().git_checkout().job_script().cf_promote_to_test().create_properties_file()

    test_release_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/test/1-release")

    test_release_base = new Base(
      jobject: test_release_job,
      slack_message: "      component: `\$component`\n      component_revision: `\$component_revision`",
      config: [
        gh_org: 'venicegeo',
        gh_repo: 'pz-release',
        gh_branch: 'test',
        slack_token: binding.variables.get("SLACK_TOKEN"),
        slack_domain: "venicegeo"
      ]
    ).defaults().github()

    test_release_steps = new Steps(
      jobject: test_release_job,
      config: config,
      jobname: 'test-release'
    ).init().gh_write().job_script().cf_release_test()

    test_promotion_job.with {
      publishers {
        downstreamParameterized {
          trigger("${config.jenkins_org}/${config.team}/${config.gh_repo}/test/1-release") {
            condition('SUCCESS')
            parameters {
              propertiesFile('pipeline.properties', true)
            }
          }
        }
      }
    }
    // -- end load test pipeline
  }

  // manual jobs
  if (entry.manual) {
    folder("${config.jenkins_org}/${config.team}/${config.gh_repo}/manual") {
      displayName("${config.gh_repo}/manual")
    }
  }

  entry.manual.eachWithIndex{ jobname, idx ->
    def manual_job = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/manual/${jobname}")

    def manual_base = new Base(
      jobject: manual_job,
      slack_message: "      component_revision: `\$component_revision`\n      domain: `\$target_domain`\n      commit sha: `\$GIT_COMMIT`",
      config: config
    ).defaults().github()

    if (!jobname.contains("promote")) {
      manual_base.parameters()
    }

    if (jobname.contains("selenium")) {
      manual_base.selenium()
    }

    def manual_steps = new Steps(
      jobject: manual_job,
      config: config,
      jobname: "${jobname}"
    ).init().git_checkout().job_script()

    if (manual_steps.metaClass.respondsTo(manual_steps, jobname)) {
      manual_steps."${jobname}"()
    }
  }
}


// -- PIAZZA AGGREGATED ROLLOUT
folder('venice/piazza/promotion') {
  displayName('promotion')
}

def production_rollout = workflowJob('venice/piazza/promotion/production')

def production_cps = ' '
entries.each{ reponame, entry ->
  if (entry.team == 'piazza' && entry.lib != true) {
    production_cps = production_cps + """
      build job: "venice/piazza/${reponame}/production/0-promote", wait: true
"""
  }
}

production_rollout.with {
  definition {
    cps {
      script(production_cps)
      sandbox()
    }
  }
}

def test_rollout = workflowJob('venice/piazza/promotion/test')

def test_cps = ' '
entries.each{ reponame, entry ->
  if (entry.team == 'piazza' && entry.lib != true) {
    test_cps = test_cps + """
      build job: "venice/piazza/${reponame}/test/0-promote", wait: true
"""
  }
}

test_rollout.with {
  definition {
    cps {
      script(test_cps)
      sandbox()
    }
  }
}
// -- END PIAZZA AGGREGATED ROLLOUT


// HACKS FOR INTEGRATION TESTS

// pz integration test repo
folder("venice/piazza/pztest-integration") {
  displayName("pztest-integration")
}

pz_gh_integration_test_job = job("venice/piazza/pztest-integration/piazza")

new Base(
  jobject: pz_gh_integration_test_job,
  config: [
    gh_org: 'venicegeo',
    gh_repo: 'pztest-integration',
    gh_branch: 'master',
    slack_token: binding.variables.get("SLACK_TOKEN"),
    slack_domain: "venicegeo",
    domains: ['test.geointservices.io', 'stage.geointservices.io', 'dev.geointservices.io', 'int.geointservices.io', 'geointservices.io'],
    domains_description: 'PCF Domain/Space to target<br>&nbsp;&nbsp;<b>geointservices.io</b>: production<br>&nbsp;&nbsp;<b>stage.geointservices.io</b>: beta<br>&nbsp;&nbsp;<b>int.geointservices.io</b>: CI<br>&nbsp;&nbsp;<b>dev.geointservices.io</b>: developer sandbox<br>&nbsp;&nbsp;<b>test.geointservices.io</b>: test bed'
  ]
).parameters().defaults().github()

def pz_gh_integration_steps = new Steps(
  jobject: pz_gh_integration_test_job,
  config: global_config,
  jobname: "blackbox"
).init().job_script().git_checkout().blackbox().gh_trigger()

// bf integration test repo
bf_gh_integration_test_job = job("venice/piazza/pztest-integration/beachfront")

new Base(
  jobject: bf_gh_integration_test_job,
  config: [
    gh_org: 'venicegeo',
    gh_repo: 'pztest-integration',
    gh_branch: 'master',
    slack_token: binding.variables.get("SLACK_TOKEN"),
    slack_domain: "venicegeo",
    domains: ['test.geointservices.io', 'stage.geointservices.io', 'dev.geointservices.io', 'int.geointservices.io', 'geointservices.io'],
    domains_description: 'PCF Domain/Space to target<br>&nbsp;&nbsp;<b>geointservices.io</b>: production<br>&nbsp;&nbsp;<b>stage.geointservices.io</b>: beta<br>&nbsp;&nbsp;<b>int.geointservices.io</b>: CI<br>&nbsp;&nbsp;<b>dev.geointservices.io</b>: developer sandbox<br>&nbsp;&nbsp;<b>test.geointservices.io</b>: test bed'
  ]
).parameters().defaults().github()

def bf_gh_integration_steps = new Steps(
  jobject: bf_gh_integration_test_job,
  config: global_config,
  jobname: "beachfront"
).init().job_script().git_checkout().blackbox().gh_trigger()
