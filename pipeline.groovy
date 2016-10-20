#!groovy                                                                           
// This is the initial seed job for venicegeo which creates all of ther            
// other pipeline jobs. Each repo must contain a JenkinsFile                       
// that denotes the steps to take when building.                                   
                                                                                   
def gitprefix = 'https://github.com/venicegeo/'                                 

// PZ Projects
def pzprojects = ['pz-access', 'pz-gateway', 'pz-idam', 'pz-ingest', 'pz-jobcommon',                
   'pz-jobmanager', 'pz-search-metadata-ingest', 'pz-search-query', 'pz-servicecontroller'] 

for(i in pzprojects) {                                                               
  pipelineJob("venic/piazza/${i}-pipeline") {                                                                                                     
  triggers {                                                                    
    gitHubPushTrigger()                                                         
  }                                                                             
  definition {                                                                  
    cpsScm {                                                                    
      scm {                                                                     
        git {                                                                   
          remote {                                                              
            url("${gitprefix}${i}")                                             
            }                                                                   
          }                                                                     
        }                                                                       
      }                                                                         
    }                                                                           
  }                                                                                
}

// BF Projects
def bfprojects = ['bf_TidePrediction']

for(i in bfprojects) {                                                               
  pipelineJob("venice/beachfront/${i}-pipeline") {                                                                                                     
  triggers {                                                                    
    gitHubPushTrigger()                                                         
  }                                                                             
  definition {                                                                  
    cpsScm {                                                                    
      scm {                                                                     
        git {                                                                   
          remote {                                                              
            url("${gitprefix}${i}")                                             
            }                                                                   
          }                                                                     
        }                                                                       
      }                                                                         
    }                                                                           
  }                                                                                
}

