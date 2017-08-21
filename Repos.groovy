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
      reponame: 'bf-api',
      team: 'beachfront'
    ],[
      reponame: 'bf-ia-broker',
      team: 'beachfront',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'zap', 'cf_bg_deploy_int', 'beachfront_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'beachfront_integration_tests_stage']
    ],[
      reponame: 'bf-swagger',
      team: 'beachfront',
      manual: ['gitlab_push'],
      pipeline: ['sonar', 'archive', 'cf_push_int', 'zap', 'cf_bg_deploy_int', 'beachfront_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage']
    ],[
      reponame: 'bf-tideprediction',
      team: 'beachfront'
    ],[
      reponame: 'bf-ui',
      team: 'beachfront'
    ],[
      reponame: 'geojson-go',
      lib: true,
      team: 'beachfront',
      manual: ['gitlab_push'],
      pipeline: ['archive']
    ],[
      reponame: 'pzsvc-exec',
      lib: true,
      team: 'beachfront',
      manual: ['gitlab_push'],
      pipeline: ['archive', 'zap']
    ],[
      reponame: 'pzsvc-lib',
      lib: true,
      team: 'beachfront',
      manual: ['gitlab_push'],
      pipeline: ['archive']
    ],[
      reponame: 'pzsvc-ndwi-py',
      team: 'beachfront',
      manual: ['archive', 'cf_push', 'cf_bg_deploy'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int', 'beachfront_integration_tests', 'cf_push_stage', 'cf_bg_deploy_stage', 'beachfront_integration_tests_stage']
    ],[
      reponame: 'pz-gateway',
      team: 'piazza'
    ],[
      reponame: 'pz-access',
      team: 'piazza'
    ],[
      reponame: 'pz-gateway',
      team: 'piazza'
    ],[
      reponame: 'pz-idam',
      team: 'piazza'
    ],[
      reponame: 'pz-ingest',
      team: 'piazza'
    ],[
      reponame: 'pz-jobcommon',
      team: 'piazza'
    ],[
      reponame: 'pz-jobmanager',
      team: 'piazza'
    ],[
      reponame: 'pz-search-metadata-ingest',
      team: 'piazza'
    ],[
      reponame: 'pz-search-query',
      team: 'piazza'
    ],[
      reponame: 'pz-servicecontroller',
      team: 'piazza'
    ],[
      reponame: 'pzsvc-preview-generator',
      team: 'piazza'
    ],[
      reponame: 'pz-gocommon',
      team: 'piazza'
    ],[
      reponame: 'pz-logger',
      team: 'piazza'
    ],[
      reponame: 'pz-workflow',
      team: 'piazza'
    ],[
      reponame: 'pzsvc-hello',
      team: 'piazza'
    ],[
      reponame: 'pz-docs',
      team: 'piazza'
    ],[
      reponame: 'pz-swagger',
      team: 'piazza'
    ],[
      reponame: 'pz-sak',
      team: 'piazza'
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
      reponame: 'gs-jupyterhub',
      team: 'sandbox',
      pipeline: ['build-rpm', 'archive'],
      branch: '**'
    ],[
      reponame: 'pz-craigflask',
      team: 'sandbox',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'pz-servicemonitor',
      team: 'sandbox',
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'example-resh1',
      team: 'sandbox',
      pipeline: ['sonar','archive', 'cf_push_int', 'cf_bg_deploy_int']
    ],[
      reponame: 'tlv',
      team: 'tlv',
      manual: ['archive', 'cf_push', 'cf_bg_deploy', 'cf_promote_to_stage', 'cf_promote_to_prod'],
      pipeline: ['archive', 'cf_push_int', 'cf_bg_deploy_int']
    ]
  ]
}
