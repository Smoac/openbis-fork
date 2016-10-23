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
        '''create data source openbis (openbis1)'''
        self.installOpenbis(instanceName ='openbis1', technologies = ['screening'])
        openbis1 = self.createOpenbisController('openbis1')
        openbis1.setDummyAuthentication()
        openbis1.setDataStoreServerUsername('etlserver1')
        openbis1.createTestDatabase('openbis')
        openbis1.createTestDatabase('pathinfo')
        openbis1.createTestDatabase('imaging')
        
        '''create harvester openbis (openbis2)'''
        self.installOpenbis(instanceName ='openbis2', technologies = ['screening', 'proteomics'])
        openbis2 = self.createOpenbisController('openbis2')
        openbis2.setDummyAuthentication()
        openbis2.setDataStoreServerUsername('etlserver2')
        openbis2.createTestDatabase('openbis')
        openbis2.createTestDatabase('pathinfo')
        openbis2.createTestDatabase('imaging')
        openbis2.createTestDatabase('proteomics')

    def executeInDevMode(self):
        openbis1 = self.createOpenbisController(instanceName = 'openbis1', dropDatabases=False)
        self.installDataSourcePlugin(openbis1)
        corePluginsPropertiesFile = "%s/servers/core-plugins/core-plugins.properties" % openbis1.installPath
        util.printAndFlush(corePluginsPropertiesFile)
        #util.writeProperties(corePluginsPropertiesFile)
        openbis1.setDataStoreServerUsername('etlserver1')
        openbis1.allUp()

        openbis2 = self.createOpenbisController(instanceName = 'openbis2', port = '8445', dropDatabases=False)
        openbis2.setDataStoreServerPort('8446')
        self.installHarvesterPlugin(openbis2)
        openbis2.setDataStoreServerUsername('etlserver2')
        openbis2.allUp()
        
    def installDataSourcePlugin(self, openbisController):
        repository = GitLabArtifactRepository(self.artifactRepository.localRepositoryFolder)
        path = repository.getPathToArtifact('149', 'archive.zip')
        util.printAndFlush("path to core plugin in the repository: %s" % path)
        destination = "%s/servers/core-plugins/openbis1/" % openbisController.installPath
        util.unzipSubfolder(path, 'openbissync.git/core-plugins/datasource/1/', destination)

    def installHarvesterPlugin(self, openbisController):
        repository = GitLabArtifactRepository(self.artifactRepository.localRepositoryFolder)
        path = repository.getPathToArtifact('149', 'archive.zip')
        util.printAndFlush("path to core plugin in the repository: %s" % path)
        destination = "%s/servers/core-plugins/openbis2/" % openbisController.installPath
        util.unzipSubfolder(path, 'openbissync.git/core-plugins/harvester/1/', destination)

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