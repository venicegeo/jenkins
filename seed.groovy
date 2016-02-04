import PipelineJob

def projects = [
  [
    name: 'refapp-devops',
    pipeline: ['setup','test','artifact','placeholder','cf-deliver','health-check','cf-deploy'],
    pcf: true,
    branch: 'master'
  ],[
    name: 'pz-alerter',
    pipeline: ['build-test-archive','cf-deliver','health-check'],
    branch: 'master'
  ],[
    name: 'pz-discover',
    pipeline: ['setup','test','artifact','cf-deliver','health-check']
  ],[
    name: 'pz-logger',
    pipeline: ['build-test-archive','cf-deliver','health-check'],
    branch: 'master'
  ],[
    name: 'pz-uuidgen',
    pipeline: ['build-test-archive','cf-deliver','health-check'],
    branch: 'master'
  ],[
    name: 'swagger-ui',
    pipeline: ['deploy']
  ],[
    name: 'pz-servicecontroller',
    pipeline: ['build', 'cf-deliver', 'health-check']
  ],[
    name: 'pz-jobcommon',
    pipeline: ['build'],
    branch: 'master'
  ],[
    name: 'pzsvc-gdaldem',
    pipeline: ['build-test-archive','cf-deliver','health-check']
  ],[
    name: 'pzsvc-lasinfo',
    pipeline: ['build-test-archive','cf-deliver','health-check']
  ],[
    name: 'pzsvc-pdal',
    pipeline: ['build-test-archive','cf-deliver','health-check']
  ],[
    name: 'pzsvc-coordinate-conversion',
    pipeline: ['build', 'cf-deliver', 'health-check']
  ],[
    name: 'pz-gateway',
    pipeline: ['build', 'cf-deliver', 'health-check'],
    branch: 'master'
  ],[
    name: 'pz-dispatcher',
    pipeline: ['build', 'cf-deliver', 'health-check'],
    branch: 'master'
  ],[
    name: 'pz-jobmanager',
    pipeline: ['build', 'cf-deliver', 'health-check'],
    branch: 'master'
  ],[
    name: 'pz-ingest',
    pipeline: ['build', 'cf-deliver'],
    branch: 'master'
  ],[    
    name: 'pzsvc-us-geospatial-filter',
    pipeline: ['build', 'cf-deliver', 'health-check']
  ],[
    name: 'pzclient-sak',
    pipeline: ['cf-deliver']
  ]
]

for (p in projects) {
  def jobs = [:]
  p.pipeline.eachWithIndex { s, i ->
    jobs[s] = new PipelineJob([
      project: p.name,
      branch: p.branch ? p.branch : '**',
      step: s,
      cfdomain: p.pcf ? 'apps.cf2.piazzageo.io' : 'cf.piazzageo.io',
      cfapi: p.pcf ? 'http://api.system.cf2.piazzageo.io' : 'http://api.cf.piazzageo.io',
      job: job("${p.name}-${s}")
    ])

    jobs[s].base()

    if (i == 0) {
      jobs[s].trigger()
    }

    switch (s) {
      case 'cf-deliver':
        jobs[s].deliver()
        break
      case 'cf-deploy':
        jobs[s].deploy()
        break
      case 'cf-teardown':
        jobs[s].teardown()
        break
    }

    if ( p.pipeline[i+1] ) {
      jobs[s].job.with {
        publishers {
          downstream("${p.name}-${p.pipeline[i+1]}", "SUCCESS")
        }
      }
    }
  }

  cleanup = new PipelineJob([
    project: p.name,
    branch: p.branch,
    step: 'cf-teardown',
    cfdomain: p.pcf ? 'apps.cf2.piazzageo.io' : 'cf.piazzageo.io',
    cfapi: p.pcf ? 'http://api.system.cf2.piazzageo.io' : 'http://api.cf.piazzageo.io',
    job: job("${p.name}-cf-teardown")
  ])

  cleanup.teardown()

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
