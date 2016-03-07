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

  p.pipeline.eachWithIndex { s, i ->

    // Base config for all Jenkins jobs
    jobs[s] = new PipelineJob([
      project: p.name,
      branch: p.branch ? p.branch : '**',
      step: s,
      cfdomain: 'devops.geointservices.io',
      cfapi: 'https://api.devops.geointservices.io',
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
          cfdomain: 'devops.geointservices.io',
          cfapi: 'https://api.devops.geointservices.io',
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
}
