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
    threadfixId: '115',
    requiresJksCreds: true
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
    threadfixId: '115',
    requiresJksCreds: true
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
  ],[
    name: 'pz-release',
    threadfixId: '115',
    requiresTagging: true
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
      stringParam("IONCHANNEL_ENDPOINT_URL", "https://api.ionchannel.io/", "URL to connect to ionchannel.")
      stringParam("GIT_URL", "https://github.com/venicegeo/${i.name}.git", "Git URL")
      stringParam("GIT_BRANCH", "master", "Default git branch")
      stringParam("PHASE_ONE_PCF_SPACE", "int", "Phase one Cloudfoundry space")
      stringParam("PHASE_ONE_PCF_DOMAIN", "int.geointservices.io", "Phase one Cloudfoundry domain")
      stringParam("PHASE_TWO_PCF_SPACE", "stage", "Phase two Cloudfoundry space")
      stringParam("PHASE_TWO_PCF_DOMAIN", "stage.geointservices.io", "Phase two Cloudfoundry domain")
      stringParam("PCF_API_ENDPOINT", "api.devops.geointservices.io", "Cloudfoundry API endpoint")
      stringParam("JAVA_BUILDPACK_NAME", "java_buildpack", "Name for the Java Buildpack")
      stringParam("PCF_ORG", "piazza", "PCF Organization")
      stringParam("THREADFIX_ID", "${i.threadfixId}", "Threadfix app id")
      stringParam("SSPF_PACKAGE", "https://github.com/venicegeo/sspf/archive/master.zip", "Security Scan Pass/Fail archive package")
      stringParam("INTEGRATION_GIT_URL", "git@gitlab.devops.geointservices.io:venicegeo/pztest-integration-source.git", "Integration Tests Git URL")
      stringParam("INTEGRATION_GIT_BRANCH", "master", "Default integration tests git branch")
      stringParam("INTEGRATION_GITLAB_CREDS", "gitlab-piazza-jenkins", "credentials for integration test repo in gitlab")
      booleanParam("SKIP_INTEGRATION_TESTS", false, "Skipping postman tests")
	  booleanParam("SKIP_SCANS", false, "Disabling the running of static/security scans")
      booleanParam("DEPLOY_PHASE_TWO", true, "Perform two phase CF deployment")
      booleanParam("SECENV", false, "Enable security banner and configurations")
      booleanParam("USE_MONGO_SSL", false, "Determines if MongoSSL should be enabled")
      if (i.requiresTagging) {
        booleanParam("TAG_AND_RELEASE", false, "Tag and release all repos to bump versions")
      }
      credentialsParam("ARTIFACT_READ_ONLY_CREDS") {
        defaultValue("NEXUS_READ_ONLY")
        description("Read-only credentials for Nexus")
      }
      credentialsParam("THREADFIX_API_KEY") {
        defaultValue("PZ_THREADFIX_API_KEY")
        description("Piazza's Threadfix API Key")
      }
      credentialsParam("SONAR_TOKEN") {
        defaultValue("sonar-publish-token")
        description("Sonar Upload Token")
      }
      credentialsParam("IONCHANNEL_SECRET_KEY") {
        defaultValue("venice_ionchannel_key")
        description("IonChannel Credentials")
      }
      credentialsParam("PCF_CREDS") {
        defaultValue("ldap_baxtersh")
        description("Cloud Foundry Credentials")
      }
      credentialsParam("POSTMAN_SECRET_FILE") {
        defaultValue("579f8660-01e6-4feb-8764-ec132432ebb1")
        description("Environment file containing credentials for Postman")
      }
      credentialsParam("ARTIFACT_STORAGE_CREDS") {
        defaultValue("nexus-deployment")
        description("Nexus Repository Credentials")
      }
      if (i.requiresJksCreds) {
        credentialsParam("JKS_FILE") {
          defaultValue("ca8591a7-fc1f-4b6d-808e-c9944c9bf4f8")
          description("Java Key Store")
        }
        stringParam("JKS_PASSPHRASE", "ff7148c6-2855-4f3d-bd2e-3aa296b09d98", "Java Key Store Passphrase")
        stringParam("PZ_PASSPHRASE", "da3092c4-d13d-4078-ab91-a630c61547aa", "PZ Passphrase")
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
def bfprojects = [
  [
    name: 'bf-tideprediction',
    threadfixId: '67',
    requires_conda: true
  ],[
    name: 'bf-ui',
    threadfixId: '63',
    requires_map_data: true,
    requires_npm: true,
    requires_xvfb: true
  ],[
    name: 'bf-swagger',
    threadfixId: '68'
  ],[
    name: 'bf-api',
    threadfixId: '57'
  ],[
    name: 'bfalg-ndwi',
    threadfixId: '92',
    requires_conda: true,
  ],[
    name: 'pzsvc-ndwi-py',
    requires_conda: true,
    childjobs: ['venicegeo-conda-recipes-pipeline']
  ],[
    name: 'bf-geojson-geopkg-converter',
    threadfixId: '117'
  ],[
    name: 'bf-ia-broker',
    threadfixId: '116',
    requires_host_urls: true
  ],[
    name: 'pzsvc-shape-py',
    requires_conda: true,
    childjobs: ['venicegeo-conda-recipes-pipeline']
  ],[
    name: 'bfalg-shape',
    threadfixId: '125',
    requires_conda: true
  ],[
    name: 'bfalg_WTA',
    requires_conda: true
  ],[
    name: 'pzsvc-wta-py',
    requires_conda: true
  ],[
    name: 'pzsvc-exec'
  ],[
    name: 'venicegeo-conda-recipes',
    requires_conda: true,
    childjobs: ['pzsvc-exec-pipeline','bfalg-ndwi-pipeline','bfalg-shape-pipeline']
  ],[
    name: 'beachfront-py',
    requires_conda: true,
    threadfixId: '91'
  ],[
    name: 'bftest-integration',
    threadfixId: '135',
    requires_npm: true
  ],[
    name: 'geojson-go',
    threadfixId: '115'
  ]
]

for(i in bfprojects) {
  pipelineJob("venice/beachfront/${i.name}-pipeline") {
    description("Beachfront pipeline")
    triggers {
      gitHubPushTrigger()
      if(i.childjobs) {
        for(job in i.childjobs) {
          upstream(job, 'SUCCESS')
        }
      }
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
      stringParam("ARTIFACT_STORAGE_DEPLOY_URL", "https://nexus.devops.geointservices.io/content/repositories/Piazza/", "Project artifact storage location for maven and others.")
      stringParam("SONAR_URL", "https://sonar.geointservices.io", "URL to upload data to sonar.")
      stringParam("IONCHANNEL_ENDPOINT_URL", "https://api.ionchannel.io/", "URL to connect to ionchannel.")
      stringParam("GIT_URL", "https://github.com/venicegeo/${i.name}.git", "Git URL")
      stringParam("GIT_BRANCH", "master", "Default git branch")
      stringParam("PHASE_ONE_PCF_SPACE", "int", "Phase one Cloudfoundry space")
      stringParam("PHASE_ONE_PCF_DOMAIN", "int.geointservices.io", "Phase one Cloudfoundry domain")
      stringParam("PHASE_TWO_PCF_SPACE", "stage", "Phase two Cloudfoundry space")
      stringParam("PHASE_TWO_PCF_DOMAIN", "stage.geointservices.io", "Phase two Cloudfoundry domain")
      stringParam("PCF_API_ENDPOINT", "api.devops.geointservices.io", "Cloudfoundry API endpoint")
      stringParam("PCF_ORG", "piazza", "PCF Organization")
      stringParam("GIT_BASE_URL", "https://github.com/venicegeo/", "Git Base URL")
      stringParam("THREADFIX_URL", "https://threadfix.devops.geointservices.io", "URL to upload data to threadfix.")
      if (i.threadfixId != null) {
       stringParam("THREADFIX_ID", "${i.threadfixId}", "Threadfix app id")
      }
      stringParam("SSPF_PACKAGE", "https://github.com/venicegeo/sspf/archive/master.zip", "Security Scan Pass/Fail archive package")
      stringParam("INTEGRATION_GIT_URL", "git@gitlab.devops.geointservices.io:venicegeo/bftest-integration-source.git", "Integration Tests Git URL")
      stringParam("INTEGRATION_GIT_BRANCH", "master", "Default integration tests git branch")
      stringParam("INTEGRATION_GITLAB_CREDS", "gitlab-piazza-jenkins", "credentials for integration test repo in gitlab")
      stringParam("JAVA_BUILDPACK_NAME", "java_buildpack", "Name for the Java Buildpack")
      stringParam("PYTHON_BUILDPACK_NAME", "python_buildpack_v1_5_18", "Name for the Python Buildpack")
      if (i.requires_host_urls) {
        stringParam("LANDSAT_HOST", "https://landsat-pds.s3.amazonaws.com", "The base URL for landsat data")
        stringParam("SENTINEL_HOST", "https://sentinel-s2-l1c.s3.amazonaws.com", "The base URL for sentinel data")
      }
      if (i.requires_xvfb) {
        booleanParam("SKIP_XVFB", true, "Disabling the xvfb dependency")
      }
      booleanParam("SKIP_INTEGRATION_TESTS", false, "Skipping postman tests")
      booleanParam("SKIP_SCANS", false, "Disabling the running of static/security scans")
      booleanParam("DEPLOY_PHASE_TWO", true, "Perform two phase CF deployment")
      booleanParam("SECENV", false, "Enable security banner and configurations")
      credentialsParam("CONSENT_BANNER_TEXT") {
        defaultValue("824eee31-0408-49e2-9a7e-70b59297b1b9")
        description("Consent banner text")
      }
      credentialsParam("SONAR_TOKEN") {
        defaultValue("sonar-publish-token")
        description("Sonar Upload Token")
      }
      credentialsParam("BF_GPKG_KEY") {
        defaultValue("bf_test_key")
        description("API Key for GeoPackage Tests")
      }
      credentialsParam("THREADFIX_API_KEY") {
        defaultValue("BF_THREADFIX_API_KEY")
        description("Beachfront's Threadfix API Key")
      }
      credentialsParam("IONCHANNEL_SECRET_KEY") {
        defaultValue("venice_ionchannel_key")
        description("IonChannel Credentials")
      }
      credentialsParam("PCF_CREDS") {
        defaultValue("ldap_baxtersh")
        description("Cloud Foundry Credentials")
      }
      credentialsParam("POSTMAN_SECRET_FILE") {
        defaultValue("579f8660-01e6-4feb-8764-ec132432ebb1")
        description("Environment file containing credentials for Postman")
      }
      credentialsParam("ARTIFACT_STORAGE_CREDS") {
        defaultValue("nexus-deployment")
        description("Nexus Repository Credentials")
      }
      credentialsParam("ARTIFACT_READ_ONLY_CREDS") {
        defaultValue("NEXUS_READ_ONLY")
        description("Read-only credentials for Nexus")
      }
      credentialsParam("BEACHFRONT_PIAZZA_AUTH"){
        defaultValue("Bf-Api-GeoAxis-PKI-Credentials")
        description("Beachfront's Piazza access key")
      }
      stringParam("GEOAXIS_DOMAIN", "gxisaccess.gxaccess.com", "Geoaxis URL")
      stringParam("PIAZZA_URL", "geointservices.io", "Piazza's URL without prefixes, which allows for the changing of spaces. Ex: piazza.{SPACE}.{PIAZZA_URL}")
      credentialsParam("GEOAXIS_CLIENT_ID") {
        defaultValue("b81d7d20-3576-4f02-ac90-4e6fd5a9d453")
      }
      credentialsParam("GEOAXIS_SECRET") {
        defaultValue("e83dfc65-4462-4a80-a04d-57ab8da20ebd")
      }
      credentialsParam("SAUCELAB_ACCESS") {
        defaultValue("1ba84f72-0a02-45e2-8869-cfa62df01251")
      }
      credentialsParam("PL_API_KEY") {
        defaultValue("7a64953f-283a-4a28-824f-4e96760574e8")
      }
      credentialsParam("GX_TEST_USER") {
        defaultValue("gx_test_account")
      }
      if(i.requiresJksCreds) {
        credentialsParam("JKS_FILE") {
          defaultValue("ca8591a7-fc1f-4b6d-808e-c9944c9bf4f8")
          description("Java Key Store")
        }
        stringParam("JKS_PASSPHRASE", "ff7148c6-2855-4f3d-bd2e-3aa296b09d98", "Java Key Store Passphrase")
        stringParam("PZ_PASSPHRASE", "da3092c4-d13d-4078-ab91-a630c61547aa", "PZ Passphrase")
      }
      if(i.requires_conda) {
        stringParam("CONDA_CHANNEL_CREDS_URL", "https://NEXUSUSER:NEXUSPASS@nexus.devops.geointservices.io/content/repositories/beachfront-conda")
        stringParam("CONDA_CHANNEL_URL", "https://nexus.devops.geointservices.io/content/repositories/beachfront-conda")
      }
      if(i.requires_map_data) {
        stringParam("OSM_BASE_URL", "osm.geointservices.io")
        stringParam("PLANET_BASE_URL", "planet.com")
      }
      if(i.requires_npm) {
        stringParam("NPM_REPOSITORY_URL", "https://nexus.devops.geointservices.io/content/repositories/npmjs/")
      }
    }
	environmentVariables {
		env("ARTIFACT_STORAGE_URL", "https://nexus.devops.geointservices.io/content/repositories/Piazza-Group/")
    }
  }
}

//Beachfront health-test job
def bfhealthprojects = ['bf-healthtest']
for(i in bfhealthprojects) {
  pipelineJob("venice/beachfront/${i}-pipeline") {
    description("Beachfront pipeline")
    triggers {
      cron('H 2 * * *')
    }
    definition {
      cpsScm {
        scriptPath("JenkinsFile.healthtest")
         scm {
           git {
             remote {
              //url("${gitprefix}${i}")
               url("${gitprefix}bftest-integration")
               branch("*/master")
            }
          }
        }
       }
      }
 parameters {
   credentialsParam("POSTMAN_SECRET_FILE") {
        defaultValue("579f8660-01e6-4feb-8764-ec132432ebb1")
        description("Environment file containing credentials for Postman")
   }     
   credentialsParam("SAUCELAB_ACCESS") {
        defaultValue("1ba84f72-0a02-45e2-8869-cfa62df01251")
   }
   credentialsParam("PL_API_KEY") {
        defaultValue("7a64953f-283a-4a28-824f-4e96760574e8")
   }  
   credentialsParam("GEOAXIS") {
         defaultValue("0efdaa83-18d6-4513-8a7c-c52ef5b07910")
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

