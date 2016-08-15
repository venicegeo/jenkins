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
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test']
    ],[
      reponame: 'bf-ui',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test']
    ],[
      reponame: 'pzsvc-ossim',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test']
    ],[
      reponame: 'pzsvc-image-catalog',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test']
    ],[
      reponame: 'pz-access',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-docs',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-gateway',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-gocommon',
      team: 'piazza',
      pipeline: ['test']
    ],[
      reponame: 'pz-ingest',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-jobcommon',
      team: 'piazza',
      pipeline: ['archive']
    ],[
      reponame: 'pz-jobmanager',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-logger',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-sak',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage'],
    ],[
      reponame: 'pz-search-metadata-ingest',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage'],
    ],[
      reponame: 'pz-search-query',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage'],
    ],[
      reponame: 'pz-security',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-servicecontroller',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-swagger',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-uuidgen',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pz-workflow',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pzsvc-hello',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pzsvc-preview-generator',
      team: 'piazza',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'integration_test', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'pzsvc-gdaldem',
      team: 'pointcloud',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', "blackbox"]
    ],[
      reponame: 'pzsvc-lasinfo',
      team: 'pointcloud',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', "blackbox"]
    ],[
      reponame: 'pzsvc-pdal',
      team: 'pointcloud',
      manual: ['cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', "blackbox"]
    ],[
      reponame: 'tlv',
      team: 'tlv',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ]
  ]
}
