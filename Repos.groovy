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

class Repos {
  static repos = [
    [
      reponame: 'bf-handle',
      team: 'beachfront',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'bf-ui',
      team: 'beachfront',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'pzsvc-ossim',
      team: 'beachfront',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'pzsvc-image-catalog',
      team: 'beachfront',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'pz-access',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-docs',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-gateway',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-gocommon',
      team: 'piazza',
      pipeline: ['test']
    ],[
      reponame: 'pz-ingest',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-jobcommon',
      team: 'piazza',
      pipeline: ['archive']
    ],[
      reponame: 'pz-jobmanager',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-logger',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-sak',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage'],
    ],[
      reponame: 'pz-search-metadata-ingest',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage'],
    ],[
      reponame: 'pz-search-query',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage'],
    ],[
      reponame: 'pz-security',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-servicecontroller',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-swagger',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-uuidgen',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-workflow',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pzsvc-hello',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pzsvc-preview-generator',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ]
  ]
}
