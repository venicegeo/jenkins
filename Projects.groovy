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

class Projects {
  static projects = [
    [
      name: 'bf-handle',
      pipeline: ['archive', 'stage', 'deploy']
    ],[
      name: 'bf-ui',
      pipeline: ['archive', 'stage', 'deploy']
    ],[
      name: 'ossim-sandbox',
      pipeline: ['archive']
    ],[
      name: 'ossim-test',
      pipeline: ['archive', 'stage', 'deploy']
    ],[
      name: 'pz-access',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-docs',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-gateway',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-gocommon',
      pipeline: ['test'],
      core: true
    ],[
      name: 'pzsvc-hello',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-ingest',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-jobcommon',
      pipeline: ['archive']
    ],[
      name: 'pz-jobmanager',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-logger',
      pipeline: ['test', 'archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-sak',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-search-metadata-ingest',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-search-query',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-security',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-servicecontroller',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-swagger',
      pipeline: ['archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-uuidgen',
      pipeline: ['test', 'archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pz-workflow',
      pipeline: ['test', 'archive', 'stage', 'deploy'],
      core: true
    ],[
      name: 'pzinfra-service-health',
      pipeline: ['static', 'test', 'archive', 'stage', 'deploy'],
    ],[
      name: 'pzsvc-gdaldem',
      pipeline: ['test', 'archive', 'stage', 'deploy', 'blackbox']
    ],[
      name: 'pzsvc-lasinfo',
      pipeline: ['test', 'archive', 'stage', 'deploy', 'blackbox']
    ],[
      name: 'pzsvc-ossim',
      pipeline: ['archive', 'stage', 'deploy']
    ],[
      name: 'pzsvc-image-catalog',
      pipeline: ['archive', 'stage', 'deploy']
    ],[
      name: 'pzsvc-pdal',
      pipeline: ['test', 'archive', 'stage', 'deploy', 'blackbox']
    ],[
      name: 'pzsvc-preview-generator',
      pipeline: ['archive', 'stage', 'deploy']
    ],[
      name: 'pztest-integration',
      pipeline: ['blackbox']
    ],[
      name: 'tlv',
      pipeline: ['archive', 'stage', 'deploy']
    ],[
      name: 'venicegeo.io',
      pipeline: ['sync']
    ]
  ]
}
