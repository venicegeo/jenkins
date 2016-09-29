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
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'beachfront_integration_tests']
    ],[
      reponame: 'bf_TidePrediction',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'beachfront_ua_tests_int', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'bf-ui',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'beachfront_ua_tests_int', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'bftest-ui',
      team: 'beachfront',
      pipeline: ['selenium'],
      manual: ['selenium']
    ],[
      reponame: 'pzsvc-ossim',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'beachfront_integration_tests']
    ],[
      reponame: 'pzsvc-image-catalog',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'beachfront_integration_tests']
    ],[
      reponame: 'pz-access',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-docs',
      team: 'piazza',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-gateway',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-gocommon',
      lib: true,
      team: 'piazza',
      pipeline: ['test']
    ],[
      reponame: 'pz-ingest',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-jobcommon',
      lib: true,
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive']
    ],[
      reponame: 'pz-jobmanager',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-logger',
      team: 'piazza',
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-sak',
      team: 'piazza',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-search-metadata-ingest',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-search-query',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-idam',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-servicecontroller',
      team: 'piazza',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-servicemonitor',
      team: 'sandbox',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'pz-swagger',
      team: 'piazza',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-uuidgen',
      team: 'piazza',
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pz-workflow',
      team: 'piazza',
      pipeline: ['test', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pzsvc-hello',
      team: 'piazza',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
    ],[
      reponame: 'pzsvc-preview-generator',
      team: 'piazza',
      pipeline: ['ionchannel_pom', 'archive', 'cf_push_int', 'cf_bg_deploy_int', 'int-release', 'run_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'stage-release']
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
    ],[
      reponame: 'gs-jupyterhub',
      team: 'sandbox',
      pipeline: ['build-rpm', 'archive'],
      branch: '**'
    ],[
      reponame: 'pz-craigflask',
      team: 'sandbox',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ]
  ]
}
