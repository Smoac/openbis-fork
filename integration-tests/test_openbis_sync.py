#!/usr/bin/python
# encoding=utf8
import os
import shutil
import settings
import systemtest.testcase
import systemtest.util as util
import urllib2

from urllib2 import Request

from systemtest.artifactrepository import GitArtifactRepository

from systemtest.testcase import TEST_DATA

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        self.installOpenbis(instanceName ='openbis1', technologies = ['screening'])
        openbis_datasource = self.createOpenbisController('openbis1')
        openbis_datasource.setDummyAuthentication()
        openbis_datasource.setDataStoreServerUsername('etlserver')
        openbis_datasource.createTestDatabase('openbis')
        openbis_datasource.createTestDatabase('pathinfo')
        openbis_datasource.createTestDatabase('imaging')
        

    def executeInDevMode(self):
        openbis_datasource = self.createOpenbisController(instanceName = 'openbis1', dropDatabases=False)
        self.installDataSourcePlugin(openbis_datasource)
        corePluginsPropertiesFile = "%s/servers/core-plugins/core-plugins.properties" % openbis_datasource.installPath
        util.printAndFlush(corePluginsPropertiesFile)
        #util.writeProperties(corePluginsPropertiesFile)
        openbis_datasource.allDown()
        openbis_datasource.setDataStoreServerUsername('etlserver')
        openbis_datasource.allUp()
        
    def installDataSourcePlugin(self, openbisController):
        repository = GitLabArtifactRepository(self.artifactRepository.localRepositoryFolder)
        path = repository.getPathToArtifact('149', 'archive.zip')
        util.printAndFlush("path to core plugin in the repository: %s" % path)
        destination = "%s/servers/core-plugins/openbis1/" % openbisController.installPath
        util.unzipSubfolder(path, 'openbissync.git/core-plugins/openbis-sync/1/', destination)

class GitLabArtifactRepository(GitArtifactRepository):
    """
    Artifact repository for a gitlab projects.
    Note: it requires project id as the project "argument". This can be found by using the following command:
    curl --header "PRIVATE-TOKEN: 2iwhxKbfe62ES8JWAKsG"  "https://ssdmsource.ethz.ch/api/v3/projects?per_page=99999"
    after logging in and retrieving the private token with
    curl https://ssdmsource.ethz.ch/api/v3/session --data-urlencode 'login=’ --data-urlencode 'password=’
    """
    def __init__(self, localRepositoryFolder, host = 'ssdmsource.ethz.ch'):
        GitArtifactRepository.__init__(self, localRepositoryFolder)
        self.host = host

    def downloadArtifact(self, project, pattern):
        url = "https://%s/api/v3/projects/%s/repository/%s" % (self.host, project, pattern)
        util.printAndFlush("Download %s to %s." % (url, self.localRepositoryFolder))
        request = Request(url, headers = {'PRIVATE-TOKEN' : '2iwhxKbfe62ES8JWAKsG'})
        self._download(urllib2.urlopen(request), pattern)
        return pattern
    
TestCase(settings, __file__).runTest()