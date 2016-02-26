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
  static list = [
    [
      name: 'pz-access',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-discover',
      pipeline: ['setup','test','artifact','cf-deliver','health-check']
    ],[
      name: 'pz-dispatcher',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-gateway',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-ingest',
      pipeline: ['build', 'cf-deliver'],
      branch: 'master'
    ],[
      name: 'pz-jobcommon',
      pipeline: ['build'],
      branch: 'master'
    ],[
      name: 'pz-jobmanager',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-logger',
      pipeline: ['build-test-archive','cf-deliver','health-check'],
      branch: 'master'
    ],[
      name: 'pz-search-metadata-ingest',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-search-query',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-servicecontroller',
      pipeline: ['build', 'cf-deliver', 'health-check', 'black-box-tests']
    ],[
      name: 'pz-swagger',
      pcf: true,
      pipeline: ['cf-deliver', 'health-check', 'cf-deploy']
    ],[
      name: 'pz-uuidgen',
      pipeline: ['build-test-archive','cf-deliver','health-check'],
      branch: 'master'
    ],[
      name: 'pz-workflow',
      pipeline: ['build-test-archive','cf-deliver','health-check'],
      branch: 'master'
    ],[
      name: 'pzclient-sak',
      pipeline: ['cf-deliver'],
      branch: 'master'
    ],[
      name: 'pzsvc-coordinate-conversion',
      pipeline: ['build', 'cf-deliver', 'health-check']
    ],[
      name: 'pzsvc-gdaldem',
      pipeline: ['build-test-archive','cf-deliver','health-check']
    ],[
     name: 'pzsvc-lasinfo',
      pipeline: ['build-test-archive','cf-deliver','health-check']
    ],[
      name: 'pzsvc-pdal',
      pipeline: ['build-test-archive','cf-deliver','health-check', 'black-box-tests']
    ],[
      name: 'pzsvc-us-geospatial-filter',
      pipeline: ['build', 'cf-deliver', 'health-check']
    ],[
      name: 'pzsvc-us-phone-number-filter',
      pipeline: ['build', 'cf-deliver', 'health-check']
    ],[
      name: 'refapp-devops',
      pipeline: ['setup','test','artifact','placeholder','cf-deliver','health-check','cf-deploy'],
      branch: 'master'
    ],[
      name: 'time-lapse-viewer',
      pipeline: ['build', 'cf-deliver', 'health-check']
    ]
  ]
}
