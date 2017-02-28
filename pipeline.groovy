#!groovy
// This is the initial seed job for venicegeo which creates all of ther
// other pipeline jobs. Each repo must contain a JenkinsFile
// that denotes the steps to take when building.

def gitprefix = 'https://github.com/venicegeo/'

// PZ Projects
def pzprojects = ['pz-access', 'pz-gateway', 'pz-idam', 'pz-ingest', 'pz-jobcommon',
   'pz-jobmanager', 'pz-search-metadata-ingest', 'pz-search-query', 'pz-servicecontroller',
   'pz-sak']

for(i in pzprojects) {
  pipelineJob("venice/piazza/${i}-pipeline") {
    description("Piazza pipeline")
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
  }
}

// BF Projects
def bfprojects = ['bf_TidePrediction', 'bf-ui', 'bf-swagger', 'bf-api']

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

