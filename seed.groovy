import PipelineJob
import ProjectMap

for (p in ProjectMap.projects) {
  def jobs = [:]

  p.pipeline.eachWithIndex { s, i ->

    // Base config for all Jenkins jobs
    jobs[s] = new PipelineJob([
      project: p.name,
      branch: p.branch ? p.branch : '**',
      step: s,
      cfdomain: p.pcf ? 'apps.cf2.piazzageo.io' : 'cf.piazzageo.io',                      // hack for 2 CF - TODO
      cfapi: p.pcf ? 'http://api.system.cf2.piazzageo.io' : 'http://api.cf.piazzageo.io', // hack for 2 CF - TODO
      job: job("${p.name}-${s}")
    ]).base()

    // If first job in the pipeline, establish an external trigger.
    if (i == 0) {
      jobs[s].trigger()
    }

    // Special keywords get special job behavior.
    switch (s) {
      case 'cf-deliver':
        jobs[s].deliver()         // deleiver to CloudFoundry
        jobs[s].triggerTeardown() // and trigger teardown on failure
        break
      case 'cf-deploy':
        jobs[s].deploy()          // Run a blue/green deployment.
        jobs[s].triggerTeardown() // and teardown on failure.
        // We need a teardown job if we are deploying.
        cleanup = new PipelineJob([
          project: p.name,
          branch: p.branch,
          step: 'cf-teardown',
          cfdomain: p.pcf ? 'apps.cf2.piazzageo.io' : 'cf.piazzageo.io',
          cfapi: p.pcf ? 'http://api.system.cf2.piazzageo.io' : 'http://api.cf.piazzageo.io',
          job: job("${p.name}-cf-teardown")
        ]).base().teardown()
        break
      case 'health-check':
        jobs[s].triggerTeardown() // A failed health-check will cause a teardown.
        break
      case 'cf-teardown':
        jobs[s].teardown() // Maybe we want a teardown in our pipeline...
        break
    }

    // This sets up our pipeline to progress when jobs are successfull.
    if ( p.pipeline[i+1] ) {
      jobs[s].job.with {
        publishers {
          downstream("${p.name}-${p.pipeline[i+1]}", "SUCCESS")
        }
      }
    }
  }

  // Create a Pipeline View for each project.
  deliveryPipelineView(p.name) {
    allowPipelineStart(true)
    allowRebuild(true)
    pipelineInstances(5)
    columns(10)
    updateInterval(60)
    pipelines {
      component(p.name, "${p.name}-${p.pipeline[0]}")
    }
  }
}
