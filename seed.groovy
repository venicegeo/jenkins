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

    // Special keywords get special job behavior.
    switch (s) {
      case 'cf-deliver':
        jobs[s].deliver()         // deleiver to CloudFoundry
        break
    }
  }
}
