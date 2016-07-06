
import lib.Base
import lib.Steps
import static Repos.repos

def entries = [:]
// rearrange the job list for processing.
for (p in repos) {
  entries[p.reponame] = [:]

  p.pipeline.eachWithIndex { jobname, idx ->
    entries[p.reponame][jobname] = [index: idx, branch: p.branch, children: [:]]

    if (p.pipeline[idx+1]) {
      entries[p.reponame][jobname].children[idx+1] = p.pipeline[idx+1]
    }
  }
}

// repo loop
entries.each{ reponame, entry ->
  def config = [
    gh_repo: reponame,
    slack_token: binding.variables.get("SLACK_TOKEN"),
    gh_org: "venicegeo",
    gh_branch: "master",
    slack_domain: "venicegeo",
    pcf_org: "piazza",
    team: "piazza",
    jenkins_org: "venice",
    nexus_org: "venice",
    envs: [
      int:    [space: 'int',             domain: 'int.geointservices.io',   api: 'https://api.devops.geointservices.io'],
      stage:  [space: 'stage',           domain: 'stage.geointservices.io', api: 'https://api.devops.geointservices.io'],
      dev:    [space: 'dev',             domain: 'dev.geointservices.io',   api: 'https://api.devops.geointservices.io'],
      test:   [space: 'test',            domain: 'test.geointservices.io',  api: 'https://api.devops.geointservices.io'],
      prod:   [space: 'prod',            domain: 'geointservices.io',       api: 'https://api.devops.geointservices.io'],
      venice: [space: 'prod',            domain: 'venicegeo.io',            api: 'https://api.venicegeo.io'            ],
    ],
    domains: ['int.geointservices.io', 'stage.geointservices.io', 'dev.geointservices.io', 'test.geointservices.io', 'geointservices.io', 'venicegeo.io'],
    domains_description: 'PCF Domain/Space to target<br>&nbsp;&nbsp;<b>geointservices.io</b>: production<br>&nbsp;&nbsp;<b>stage.geointservices.io</b>: beta<br>&nbsp;&nbsp;<b>int.geointservices.io</b>: CI<br>&nbsp;&nbsp;<b>dev.geointservices.io</b>: developer sandbox<br>&nbsp;&nbsp;<b>test.geointservices.io</b>: test bed<br>&nbsp;&nbsp;<b>venicegeo.io</b>: OSS Production'
  ]

  folder("${config.team}/${config.gh_repo}") {
    displayName("${config.team}/${config.gh_repo}")
  }

  // job loop
  entry.each{ jobname, data ->
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

      mutant = workflowJob("${config.team}/${config.gh_repo}/${data.index}-integration_test").with {
        definition {
          cps {
            script("""
              build job: "${config.team}/integration_test", wait: true
            """)
            sandbox()
          }
        }
      }

    } else {

      mutant = job("${config.team}/${config.gh_repo}/${data.index}-${jobname}")

      new Base(
        jobject: mutant,
        config: config
      ).defaults().parameters()

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
  cf_push_job = job("${config.team}/${config.gh_repo}/manual/cf_push")

  new Base(
    jobject: cf_push_job,
    config: config
  ).defaults().parameters()

  def cf_push_steps = new Steps(
    jobject: cf_push_job,
    config: config,
    jobname: "cf_push"
  ).init().defaults()

  cf_push_steps.cf_push()

  bg_deploy_job = job("${config.team}/${config.gh_repo}/manual/bg_deploy")

  new Base(
    jobject: bg_deploy_job,
    config: config
  ).defaults().parameters()

  def bg_deploy_steps = new Steps(
    jobject: bg_deploy_job,
    config: config,
    jobname: "bg_deploy"
  ).init().defaults()

  bg_deploy_steps.bg_deploy()
}