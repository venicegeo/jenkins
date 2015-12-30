import PipelineJob

def projects = [
  [
    name: 'refapp-devops',
    pipeline: ['setup','test','artifact','placeholder','cf-deliver','health-check']
  ]
]

for (p in projects) {
  def jobs = [:]
  p.pipeline.eachWithIndex { s, i ->
    jobs[s] = new PipelineJob([
      project: p.name,
      step: s,
      job: job("${p.name}-${s}")
    ])

    jobs[s].base()

    if (i == 0) {
      jobs[s].trigger()
    }

    if (s == 'cf-deliver') {
      jobs[s].deliver()
    }

    if ( p.pipeline[i+1] ) {
      jobs[s].job.with {
        publishers {
          downstream("${p.name}-${p.pipeline[i+1]}", "SUCCESS")
        }
      }
    }
  }

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
