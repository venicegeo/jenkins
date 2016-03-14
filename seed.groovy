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

import PiazzaJob
import static Projects.projects

def entries = [:]

def SLACK_TOKEN = binding.variables.get("SLACK_TOKEN")

// rearrange the job list for processing.
for (p in projects) {
  entries[p.name] = [:]

  p.pipeline.eachWithIndex { step, idx ->
    entries[p.name][step] = [index: idx, branch: p.branch, children: [:]]

    if (p.pipeline[idx+1]) {
      entries[p.name][step].children[idx+1] = p.pipeline[idx+1]
    }
  }
}

entries.each{ name, entry ->

  // create parent folder
  folder(name) {
    displayName(name)
  }

  entry.each{ step, data ->

    // base job config
    data.config = new PiazzaJob([
      jobject: job("${name}/${data.index}-${step}"),
      reponame: name,
      targetbranch: data.branch ? data.branch : 'master',
      slackToken: SLACK_TOKEN,
      script: step
    ]).base()

    // first job in pipeline needs an external trigger.
    if (data.index == 0) {
      data.config.trigger()
      // construct the pipeline view
      buildPipelineView("piazza/${name}/pipeline") {
        filterBuildQueue()
        filterExecutors()
        title("${name} build pipeline")
        displayedBuilds(5)
        selectedJob("piazza/${name}/${data.index}-${step}")
        alwaysAllowManualTrigger()
        showPipelineParameters()
        refreshFrequency(60)
      }
    }

    // define downstream jobs
    if (data.children) {
      data.children.each { idx, childname ->
        data.config.downstream("${idx}-${childname}")
      }
    }

    // Special keywords get special job behavior.
    switch (step) {
      case 'archive':
        data.config.archive()       // push artifact to nexus
        break
      case 'stage':
        data.config.stage()         // stage artifact in PCF
        break
      case 'deploy':
        data.config.deploy()         // blue/green deploy in PCF (credentials needed)
        break
    }
  }
}
