#!groovy
// This is the initial seed job for venicegeo which creates all of ther
// other pipeline jobs. Each repo must contain a JenkinsFile
// that denotes the steps to take when building.


folder("venice/piazza") {
  displayName("piazza")
}

def gitprefix = 'https://github.com/venicegeo/'

// PZ Projects
def pzprojects = [
  [
    name: 'pz-access',
    threadfixId: '115'
  ],[
    name: 'pz-docs',
    threadfixId: '115'
  ],[
    name: 'pz-gateway',
    threadfixId: '115'
  ],[
    name: 'pz-gocommon',
    threadfixId: '115'
  ],[
    name: 'pz-idam',
    threadfixId: '115'
  ],[
    name: 'pz-ingest',
    threadfixId: '115'
  ],[
    name: 'pz-jobcommon',
    threadfixId: '115'
  ],[
    name: 'pz-jobmanager',
    threadfixId: '115'
  ],[
    name: 'pz-logger',
    threadfixId: '115'
  ],[
    name: 'pz-search-metadata-ingest',
    threadfixId: '115'
  ],[
    name: 'pz-search-query',
    threadfixId: '115'
  ],[
    name: 'pz-servicecontroller',
    threadfixId: '115'
  ],[
    name: 'pz-sak',
    threadfixId: '115'
  ],[
    name: 'pz-swagger',
    threadfixId: '115'
  ],[
    name: 'pz-workflow',
    threadfixId: '115'
  ],[
    name: 'pzsvc-hello',
    threadfixId: '115'
  ],[
    name: 'pzsvc-preview-generator',
    threadfixId: '115'
  ],[
    name: 'pztest-integration',
    threadfixId: '115'
  ]
]

for(i in pzprojects) {
  pipelineJob("venice/piazza/${i.name}-pipeline") {
    description("Piazza pipeline")
    triggers {
      gitHubPushTrigger()
    }
    environmentVariables {
      env('THREADFIX_ID', i.threadfixId)
      env('INT_CF_DOMAIN', 'int.geointservices.io')
    }
    definition {
      cpsScm {
        scm {
          git {
            remote {
              url("${gitprefix}${i.name}")
              branch("*/master")
            }
          }
        }
      }
    }
    parameters {
      //stringParam("ARTIFACT_STORAGE_URL", "https://nexus.devops.geointservices.io/content/repositories/Piazza-Group/", "Artifact storage location for external maven dependencies.")
      stringParam("ARTIFACT_STORAGE_DEPLOY_URL", "https://nexus.devops.geointservices.io/content/repositories/Piazza/", "Project artifact storage location for maven and others.")
      stringParam("THREADFIX_URL", "https://threadfix.devops.geointservices.io", "URL to upload data to threadfix.")
      stringParam("SONAR_URL", "https://sonar.geointservices.io", "URL to upload data to sonar.")
      stringParam("GIT_URL", "https://github.com/venicegeo/${i.name}.git", "Git URL")
      stringParam("GIT_BRANCH", "master", "Default git branch")
      stringParam("PHASE_ONE_PCF_SPACE", "int", "Phase one Cloudfoundry space")
      stringParam("PHASE_ONE_PCF_DOMAIN", "int.geointservices.io", "Phase one Cloudfoundry domain")
      stringParam("PHASE_TWO_PCF_SPACE", "stage", "Phase two Cloudfoundry space")
      stringParam("PHASE_TWO_PCF_DOMAIN", "stage.geointservices.io", "Phase two Cloudfoundry domain")
      stringParam("PCF_API_ENDPOINT", "api.devops.geointservices.io", "Cloudfoundry API endpoint")
      stringParam("PCF_ORG", "piazza", "PCF Organization")
      stringParam("THREADFIX_ID", "${i.threadfixId}", "Threadfix app id")
      booleanParam("SKIP_INTEGRATION_TESTS", false, "Skipping postman tests")
      booleanParam("DEPLOY_PHASE_TWO", true, "Perform two phase CF deployment")
      credentialsParam("THREADFIX_API_KEY") {
        defaultValue("PZ_THREADFIX_API_KEY")
        description("Piazza's Threadfix API Key")
      }
      credentialsParam("SONAR_TOKEN") {
        defaultValue("sonar-publish-token")
        description("Sonar Upload Token")
      }
    }
    environmentVariables {
      env("ARTIFACT_STORAGE_URL", "https://nexus.devops.geointservices.io/content/repositories/Piazza-Group/")
    }
  }
}

folder("venice/beachfront") {
  displayName("beachfront")
}

// BF Projects
def bfprojects = ['bf_TidePrediction', 'bf-ui', 'bf-swagger', 'bf-api', 'pzsvc-ndwi-py', 'bf-geojson-geopkg-converter']

for(i in bfprojects) {
  pipelineJob("venice/beachfront/${i}-pipeline") {
    description("Beachfront pipeline")
    triggers {
      gitHubPushTrigger()
    }
    definition {
      cpsScm {
        scm {
          git {
            remote {
              url("${gitprefix}${i}")
              branch("*/master")
            }
          }
       }
     }
   }
   parameters {
     stringParam("ARTIFACT_STORAGE_URL", "https://nexus.devops.geointservices.io/content/repositories/Piazza-Group/", "Artifact storage location for Maven and others.")
     stringParam("THREADFIX_URL", "https://threadfix.devops.geointservices.io", "URL to upload data to threadfix.")
   }

  }
}

//Beachfront health-test job
def bfhealthprojects = ['bftest-integration']
for(i in bfhealthprojects) {
  pipelineJob("venice/beachfront/${i}-pipeline") {
    description("Beachfront pipeline")
    triggers {
      cron('H 8 * * *')
    }
    definition {
      cpsScm {
        scm {
          git {
            remote {
              url("${gitprefix}${i}")
              branch("*/master")
            }
          }
        }
     }
   }
 }
}
// Boundless Projects
//def boundlessgitprefix = 'https://github.com/boundlessgeo/'
//
//def boundlessprojects = ['exchange', 'storyscapes', 'registry']
//
//for(i in boundlessprojects) {
//  pipelineJob("venice/boundless/${i}-pipeline") {
//    description("Boundless security pipeline")
//    triggers {
//      gitHubPushTrigger()
//    }
//    definition {
//      cpsScm {
//        scm {
//          git {
//            remote {
//              url("${boundlessgitprefix}${i}")
//              branch("*/master")
//            }
//          }
//       }
//     }
//   }
//  }
//}

