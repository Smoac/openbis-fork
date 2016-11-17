#!/usr/bin/python
# encoding=utf8
import os
import shutil
import settings
import systemtest.testcase
import systemtest.util as util
import urllib, urllib2
import ssl, base64

from urllib2 import Request

from functools import wraps

from systemtest.artifactrepository import GitArtifactRepository

from systemtest.testcase import TEST_DATA

#Had to add the ssl wrap thing below because of a problem during the auth call
def sslwrap(func):
    @wraps(func)
    def bar(*args, **kw):
        kw['ssl_version'] = ssl.PROTOCOL_TLSv1
        return func(*args, **kw)
    return bar
ssl.wrap_socket = sslwrap(ssl.wrap_socket)

class TestCase(systemtest.testcase.TestCase):

    def execute(self):
        openbis1_dss_port = '8444'
        openbis2_port = '8445'
        openbis2_dss_port = '8446'

        '''create data source openbis (openbis1)'''
        self.installOpenbis(instanceName ='openbis1', technologies = ['screening'])
        openbis1 = self.createOpenbisController('openbis1')
        openbis1.setDummyAuthentication()
        openbis1.setDataStoreServerUsername('etlserver1')
        openbis1.setDataStoreServerProperty("host-address", "https://localhost")
        openbis1.createTestDatabase('openbis')
        openbis1.createTestDatabase('pathinfo')
        openbis1.createTestDatabase('imaging')
        
        '''Set openbis1 as the datasource'''
        self.installDataSourcePlugin(openbis1, openbis1_dss_port)
        self.installEntityRegistrationPlugin(openbis1)
        corePluginsPropertiesFile = "%s/servers/core-plugins/core-plugins.properties" % openbis1.installPath
        util.printAndFlush(corePluginsPropertiesFile)
        openbis1.allUp()
        
        '''Drop the folder to register some test entities in space SYNC'''
        openbis1.dropAndWait("ENTITY_REGISTRATION", "openbis-sync-entity-reg")
        
        '''create harvester openbis (openbis2)'''
        self.installOpenbis(instanceName ='openbis2', technologies = ['screening', 'proteomics'])
        openbis2 = self.createOpenbisController('openbis2', port = openbis2_port)
        openbis2.setDummyAuthentication()
        openbis2.setDataStoreServerUsername('etlserver2')
        openbis2.setDataStoreServerPort(openbis2_dss_port)
        openbis2.setOpenbisPortDataStoreServer(openbis2_port)
        openbis2.setDataStoreServerProperty("host-address", "https://localhost")
        openbis2.createTestDatabase('openbis')
        openbis2.createTestDatabase('pathinfo')
        openbis2.createTestDatabase('imaging')
        openbis2.createTestDatabase('proteomics')

        '''set openbis2 as harvester'''
        self.installHarvesterPlugin(openbis2)
        source = self.getHarvesterConfigFolder()
        destination = openbis2.installPath
        util.printAndFlush("Copying harvester configuration file from %s to %s"% (source, destination))
        util.copyFromTo(source, destination, "harvester-config.txt")

        '''datasource plugin is installed on harvester side as well in order to get the resource list during testing'''
        self.installDataSourcePlugin(openbis2, openbis2_dss_port)

        openbis2.allUp()
        
        monitor = util.LogMonitor("%s syncronization.log" % openbis2.instanceName,
                                  "%s/syncronization.log" % openbis2.installPath) # "%s/servers/datastore_server/log/datastore_server_log.txt" % openbis2.installPath
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.DataSetRegistrationTask'))
        monitor.waitUntilEvent(util.RegexCondition('OPERATION.DataSetRegistrationTask - Saving the timestamp of sync start to file'))
        
        '''read entity graph from datasource'''
        datasource_graph_response = self.getResourceListForComparison('8444', 'harvester1', '123')
        file1 = os.path.join(destination, "datasource_graph.txt")
        self.writeResponseToFile(datasource_graph_response, file1)
        content1 = self.readLinesFromFile(file1)
        content1.sort()

        '''read entity graph from harvester'''
        harvester_graph_response = self.getResourceListForComparison(openbis2_dss_port, 'testuser1', '123')
        file2 = os.path.join(destination, "harvester_graph.txt")
        self.writeResponseToFile(harvester_graph_response, file2)
        content2 = self.readLinesFromFile(file2)
        content2.sort()
        
        '''compare the two. If the only difference is in space labels then we are good.'''
        diff_array = self.diff(set(content1), set(content2))
        same = True
        with open(os.path.join(destination, "diff.txt"), 'wb') as output:
            for item in diff_array:
                output.write("%s\n" % item)
                if item.startswith("label") == False:
                    same = False
        self.assertEquals("The entity graphs on datasource and harvester are equal", True, same)

        if same == False:
            self.fail("The entity graphs on datasource and harvester are not equal.See %s for details" % os.path.join(destination, "diff.txt"))
            
    def executeInDevMode(self):
        openbis1_dss_port = '8444'
        openbis1 = self.createOpenbisController(instanceName = 'openbis1', dropDatabases=False)
        openbis1.setDummyAuthentication()
        self.installDataSourcePlugin(openbis1, openbis1_dss_port)
        self.installEntityRegistrationPlugin(openbis1)
        corePluginsPropertiesFile = "%s/servers/core-plugins/core-plugins.properties" % openbis1.installPath
        util.printAndFlush(corePluginsPropertiesFile)
        #util.writeProperties(corePluginsPropertiesFile)
        openbis1.setDataStoreServerUsername('etlserver1')
        openbis1.setDataStoreServerProperty("host-address", "https://localhost")
        openbis1.allUp()
 
        '''uncomment the following if we have ot run the test once in non-dev mode before (otherwise we already have ENTITY_REGISTRATION and get an error'''
#        '''Drop the folder to register some test entities in space SYNC'''
#        openbis1.dropAndWait("ENTITY_REGISTRATION", "openbis-sync-entity-reg")

        openbis2_port = '8445'
        openbis2_dss_port = '8446'
        openbis2 = self.createOpenbisController(instanceName = 'openbis2', port = openbis2_port, dropDatabases=False)
        openbis2.setDataStoreServerPort(openbis2_dss_port)
        openbis2.setOpenbisPortDataStoreServer(openbis2_port)
        openbis2.setDataStoreServerProperty("host-address", "https://localhost")
        self.installHarvesterPlugin(openbis2)
        openbis2.setDummyAuthentication()
        '''datasource plugin is installed on harvester side as well just for testing'''
        self.installDataSourcePlugin(openbis2, openbis2_dss_port)
        openbis2.setDataStoreServerUsername('etlserver2')
        source = self.getHarvesterConfigFolder()
        destination = openbis2.installPath
        util.printAndFlush("Copying harvester configuration file from %s to %s"% (source, destination))
        util.copyFromTo(source, destination, "harvester-config.txt")
        openbis2.allUp()
        
        monitor = util.LogMonitor("%s syncronization.log" % openbis2.instanceName,
                                  "%s/syncronization.log" % openbis2.installPath) # "%s/servers/datastore_server/log/datastore_server_log.txt" % openbis2.installPath
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.DataSetRegistrationTask'))
        monitor.waitUntilEvent(util.RegexCondition('OPERATION.DataSetRegistrationTask - Saving the timestamp of sync start to file'))
        
        '''read entity graph from datasource'''
        datasource_graph_response = self.getResourceListForComparison('8444', 'harvester1', '123')
        file1 = os.path.join(destination, "datasource_graph.txt")
        self.writeResponseToFile(datasource_graph_response, file1)
        content1 = self.readLinesFromFile(file1)
        content1.sort()

        '''read entity graph from harvester'''
        harvester_graph_response = self.getResourceListForComparison(openbis2_dss_port, 'testuser1', '123')
        file2 = os.path.join(destination, "harvester_graph.txt")
        self.writeResponseToFile(harvester_graph_response, file2)
        content2 = self.readLinesFromFile(file2)
        content2.sort()
        
        '''compare the two. If the only difference is in space labels then we are good.'''
        diff_array = self.diff(set(content1), set(content2))
        same = True
        with open(os.path.join(destination, "diff.txt"), 'wb') as output:
            for item in diff_array:
                output.write("%s\n" % item)
                if item.startswith("label") == False:
                    same = False
        self.assertEquals("The entity graphs on datasource and harvester are not equal", True, same)

        if same == False:
            self.fail("The entity graphs on datasource and harvester are not equal.See %s for details" % os.path.join(destination, "diff.txt"))
                
    def readLinesFromFile(self, file):
        with open(file, 'rb') as output:
            content = output.readlines()
        return content
            
    def writeResponseToFile(self, datasource_graph_response, file1):
        with open(file1, 'wb') as output:
            output.write(datasource_graph_response.read())
        return output
    
    def diff(self, first, second):
        first = set(first)
        second = set(second)
        return [item for item in first if item not in second]
        
    def getResourceListForComparison(self, dss_port, user, password):
        url = "https://localhost:%s/datastore_server/re-sync?verb=resourcelist.xml" % dss_port
        request = urllib2.Request(url)
        request.add_header('Accept', 'application/json')
        request.add_header("Content-type", "application/x-www-form-urlencoded")
        base64string = base64.encodestring('%s:%s' % (user, password)).replace('\n', '')
        request.add_header("Authorization", "Basic %s" % base64string)
        data = urllib.urlencode({'mode' : 'test'})
        response = urllib2.urlopen(request, data)
        return response

    def installPlugin(self, openbisController, plugin_name):
        repository = GitLabArtifactRepository(self.artifactRepository.localRepositoryFolder)
        path = repository.getPathToArtifact('149', 'archive.zip')
        util.printAndFlush("path to core plugin in the repository: %s" % path)
        destination = "%s/servers/core-plugins/%s/" % (openbisController.installPath, openbisController.instanceName)
        util.printAndFlush("Unzipping plugin % s into folder %s"% (plugin_name, destination))
        util.unzipSubfolder(path, 'openbissync.git/core-plugins/%s/1/'% plugin_name, destination)

    def installDataSourcePlugin(self, openbisController, dss_port):
        self.installPlugin(openbisController, "datasource")
        datasource_core_plugin_properties = "%s/1/dss/services/resource-sync/plugin.properties" % openbisController.instanceName
        plugin_properties_file = os.path.join(openbisController.installPath, "servers", "core-plugins", datasource_core_plugin_properties)
        util.printAndFlush("Updating %s" % plugin_properties_file)
        pluginProps = util.readProperties(plugin_properties_file)
        pluginProps['request-handler.server-url'] = "https://localhost:%s/openbis" % dss_port
        pluginProps['request-handler.download-url'] = "https://localhost:%s" % dss_port
        util.writeProperties(plugin_properties_file, pluginProps)
        
    def installEntityRegistrationPlugin(self, openbisController):
        self.installPlugin(openbisController, "test")

    def installHarvesterPlugin(self, openbisController):
        self.installPlugin(openbisController, "harvester")

    def getHarvesterConfigFolder(self):
        return systemtest.testcase.TEMPLATES + "/" + self.name + "/harvester_config"
    
class GitLabArtifactRepository(GitArtifactRepository):
    """
    Artifact repository for a gitlab projects.
    Note: it requires project id as the project "argument". This can be found by using the following command:
    curl --header "PRIVATE-TOKEN: Wbt8EV8pREkqwu3BqQtQ"  "https://ssdmsource.ethz.ch/api/v3/projects?per_page=99999"
    after logging in and retrieving the private token with
    curl https://ssdmsource.ethz.ch/api/v3/session --data-urlencode 'login=’ --data-urlencode 'password=’
    """
    def __init__(self, localRepositoryFolder, host = 'ssdmsource.ethz.ch'):
        GitArtifactRepository.__init__(self, localRepositoryFolder)
        self.host = host

    def downloadArtifact(self, project, pattern):
        url = "https://%s/api/v3/projects/%s/repository/%s" % (self.host, project, pattern)
        util.printAndFlush("Download %s to %s." % (url, self.localRepositoryFolder))
        request = Request(url, headers = {'PRIVATE-TOKEN' : 'Wbt8EV8pREkqwu3BqQtQ'})
        self._download(urllib2.urlopen(request), pattern)
        return pattern
    
TestCase(settings, __file__).runTest()