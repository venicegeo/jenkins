// Copyright 2016, RadiantBlue Technologies, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import PipelineJob
import Projects

for (p in Projects.list) {
  def jobs = [:]

  folder(p.name) {
    displayName(p.name)
  }

  p.pipeline.eachWithIndex { s, i ->

    // Base config for all Jenkins jobs
    jobs[s] = new PipelineJob([
      project: p.name,
      branch: p.branch ? p.branch : 'master',
      step: s,
      cfdomain: 'stage.geointservices.io',
      cfapi: 'https://api.devops.geointservices.io',
      slackToken: binding.variables.get("SLACK_TOKEN"),
      job: job("${p.name}/${i}-${s}")
    ]).base()

    // If first job in the pipeline, establish an external trigger.
    if (i == 0) {
      jobs[s].trigger()
    }

    // Special keywords get special job behavior.
    switch (s) {
      case 'archive':
        jobs[s].archive()         // push artifact to nexus
        break
      case 'stage':
        jobs[s].deliver()         // pull artifact from nexus, stage in PCF
        break
      case 'deploy':
        jobs[s].deliver()         // Blue/Green deploy on PCF
        break
    }

    // This sets up our pipeline to progress when jobs are successfull.
    if ( p.pipeline[i+1] ) {
      jobs[s].job.with {
        configure { project ->
          project / publishers << 'hudson.tasks.BuildTrigger' {
            childProjects "piazza/${p.name}/${i+1}-${p.pipeline[i+1]}"
            threshold {
              name "SUCCESS"
              ordinal "0"
              color "BLUE"
              completeBuild true
            }
          }
        }
      }
    }
  }

  buildPipelineView("piazza/${p.name}/pipeline") {
    filterBuildQueue()
    filterExecutors()
    title("${p.name} pipeline")
    displayedBuilds(5)
    selectedJob("piazza/${p.name}/0-${p.pipeline[0]}")
    alwaysAllowManualTrigger()
    showPipelineParameters()
    refreshFrequency(60)
  }
}
