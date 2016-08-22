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

    if (jobname == "run_integration_tests") {
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

    } else if (jobname.contains("release")) {
      base_job = new Base(
        jobject: mutant,
        config: [
          gh_org: 'venicegeo',
          gh_repo: 'pz-release',
          gh_branch: (jobname.contains("stage")) ? 'rc' : 'ci',
          slack_token: binding.variables.get("SLACK_TOKEN"),
          slack_domain: "venicegeo"
        ]
      ).defaults().github()

      steps.gh_write()

    } else {
      // Do not checkout integration tests
      steps.git_checkout()

      base_job = new Base(
        jobject: mutant,
        slack_message: "      commit sha: `\$GIT_COMMIT`",
        config: config
      ).defaults().github()
    }

    steps.job_script()

    if (steps.metaClass.respondsTo(steps, jobname)) {
      steps."${jobname}"()
    }

    if (jobname == "release") {
      steps.cf_release_stage()
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

  // manual jobs
  folder("${config.jenkins_org}/${config.team}/${config.gh_repo}/manual") {
    displayName("${config.gh_repo}/manual")
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
