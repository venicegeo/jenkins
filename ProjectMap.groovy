class ProjectMap {
  static projects = [
    [
      name: 'refapp-devops',
      pipeline: ['setup','test','artifact','placeholder','cf-deliver','health-check','cf-deploy'],
      pcf: true,
      branch: 'master'
    ],[
      name: 'pz-alerter',
      pipeline: ['build-test-archive','cf-deliver','health-check'],
      branch: 'master'
    ],[
      name: 'pz-discover',
      pipeline: ['setup','test','artifact','cf-deliver','health-check']
    ],[
      name: 'pz-logger',
      pipeline: ['build-test-archive','cf-deliver','health-check'],
      branch: 'master'
    ],[
      name: 'pz-uuidgen',
      pipeline: ['build-test-archive','cf-deliver','health-check'],
      branch: 'master'
    ],[
      name: 'swagger-ui',
      pcf: true,
      pipeline: ['cf-deliver']
    ],[
      name: 'pz-servicecontroller',
      pipeline: ['build', 'cf-deliver', 'health-check']
    ],[
      name: 'pz-jobcommon',
      pipeline: ['build'],
      branch: 'master'
    ],[
      name: 'pzsvc-gdaldem',
      pipeline: ['build-test-archive','cf-deliver','health-check']
    ],[
      name: 'pzsvc-lasinfo',
      pipeline: ['build-test-archive','cf-deliver','health-check']
    ],[
      name: 'pzsvc-pdal',
      pipeline: ['build-test-archive','cf-deliver','health-check']
    ],[
      name: 'pzsvc-coordinate-conversion',
      pipeline: ['build', 'cf-deliver', 'health-check']
    ],[
      name: 'pz-gateway',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-dispatcher',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-jobmanager',
      pipeline: ['build', 'cf-deliver', 'health-check'],
      branch: 'master'
    ],[
      name: 'pz-ingest',
      pipeline: ['build', 'cf-deliver'],
      branch: 'master'
    ],[    
      name: 'pzsvc-us-geospatial-filter',
      pipeline: ['build', 'cf-deliver', 'health-check']
    ],[
      name: 'pzclient-sak',
      pipeline: ['cf-deliver']
    ]
  ]
}
