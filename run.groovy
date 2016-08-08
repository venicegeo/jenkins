import lib.Base
import lib.Steps
import static Repos.repos

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
    envs: [
      int:    [space: 'int',             domain: 'int.geointservices.io',   api: 'https://api.devops.geointservices.io'],
      stage:  [space: 'stage',           domain: 'stage.geointservices.io', api: 'https://api.devops.geointservices.io'],
      dev:    [space: 'dev',             domain: 'dev.geointservices.io',   api: 'https://api.devops.geointservices.io'],
      test:   [space: 'test',            domain: 'test.geointservices.io',  api: 'https://api.devops.geointservices.io'],
      prod:   [space: 'prod',            domain: 'geointservices.io',       api: 'https://api.devops.geointservices.io']
    ],
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
    def mutant

    // construct the pipeline view
    if (data.index == 0) {
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
    }


    if (jobname == "integration_test") {

      mutant = workflowJob("${config.jenkins_org}/${config.team}/${config.gh_repo}/${data.index}-integration_test")

      mutant.with {
        definition {
          cps {
            script("""
              build job: "${config.jenkins_org}/${config.team}/integration_test", wait: true
            """)
            sandbox()
          }
        }
      }

    } else {

      mutant = job("${config.jenkins_org}/${config.team}/${config.gh_repo}/${data.index}-${jobname}")

      new Base(
        jobject: mutant,
        config: config
      ).defaults().github().parameters()

      def steps = new Steps(
        jobject: mutant,
        config: config,
        jobname: jobname
      ).init().defaults()

      if (steps.metaClass.respondsTo(steps, jobname)) {
        steps."${jobname}"()
      }

      // first job in pipeline needs an external trigger.
      if (data.index == 0) {
        steps.gh_trigger()
      }

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
                  predefinedProp('revision', '$revision')
                  predefinedProp('domain', '$domain')
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

    new Base(
      jobject: manual_job,
      config: config
    ).defaults().github().parameters()

    def manual_steps = new Steps(
      jobject: manual_job,
      config: config,
      jobname: "cf_push"
    ).init().defaults()

    if (manual_steps.metaClass.respondsTo(manual_steps, jobname)) {
      manual_steps."${jobname}"()
    }
  }
}


// Piazza integration test
integration_test_job = job("venice/piazza/integration_test")

new Base(
  jobject: integration_test_job,
  config: [
    gh_org: 'venicegeo',
    gh_repo: 'pztest-integration'
    gh_branch: 'master'
  ]
).github()

def integration_steps = new Steps(
  jobject: integration_test_job,
  config: [],
  jobname: "blackbox"
).init().defaults().blackbox()
