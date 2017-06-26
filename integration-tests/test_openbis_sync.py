#!/usr/bin/python
# encoding=utf8
import os
import re
import shutil
import settings
import systemtest.testcase
import systemtest.util as util
import urllib, urllib2
import ssl, base64
import json

from urllib2 import Request

from functools import wraps

from systemtest.artifactrepository import GitArtifactRepository

from systemtest.testcase import TEST_DATA
from systemtest.util import printAndFlush

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
        
        '''Copy master data script'''
        filePath = "%s/servers/core-plugins/%s/1/as" % (openbis1.installPath, openbis1.instanceName)
        os.makedirs(filePath)
        util.printAndFlush("Copying master data script from %s to %s" % (self.getMasterDataScriptFolder(), filePath))
        util.copyFromTo(self.getMasterDataScriptFolder(), filePath, "initialize-master-data.py")
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
        
        harvester_config_file_name= "harvester-config.txt"
        data_source_alias = self.extractDataSourceAlias(os.path.join(source, harvester_config_file_name))
        util.printAndFlush("data source alias is: %s" % data_source_alias)

        destination = openbis2.installPath
        util.printAndFlush("Copying harvester configuration file from %s to %s"% (source, destination))
        util.copyFromTo(source, destination, harvester_config_file_name)

        '''datasource plugin is installed on harvester side as well in order to get the resource list during testing'''
        self.installDataSourcePlugin(openbis2, openbis2_dss_port)

        source = self.getHarvesterConfigFolder()
         
        harvester_config_file_name= "harvester-config.txt"
        harvester_config_file_path = os.path.join(source, harvester_config_file_name)
        harvester_config_lines =  self.readLinesFromFile(harvester_config_file_path)
         
        data_source_alias = self.extractDataSourceAlias(harvester_config_lines)
        util.printAndFlush("Data source alias is: %s" % data_source_alias)
  
        destination =  openbis2.installPath
        util.printAndFlush("Copying harvester configuration file from %s to %s"% (source, destination))
        util.copyFromTo(source, destination, harvester_config_file_name)
       
        '''first do a dry run'''
        self.dry_run(openbis2_dss_port, openbis2, harvester_config_file_name, harvester_config_lines)
        
        '''then do a real run'''
        self.realRun(openbis1, openbis2_dss_port, openbis2, source, harvester_config_file_name, data_source_alias)

    def executeInDevMode(self):
        openbis1_dss_port = '8444'
        openbis2_port = '8445'
        openbis2_dss_port = '8446'

        openbis1=self.createOpenbisController(instanceName = 'openbis1', dropDatabases=False)
        openbis1.setDummyAuthentication()
        self.installDataSourcePlugin(openbis1, openbis1_dss_port)
         
        '''Copy master data script'''        
        filePath = "%s/servers/core-plugins/%s/1/as" % (openbis1.installPath, openbis1.instanceName)
        os.makedirs(filePath)
        util.printAndFlush("Copying master data script from %s to %s" %(self.getMasterDataScriptFolder(), filePath ))
        util.copyFromTo(self.getMasterDataScriptFolder(), filePath, "initialize-master-data.py")
 
        corePluginsPropertiesFile = "%s/servers/core-plugins/core-plugins.properties" % openbis1.installPath
        util.printAndFlush(corePluginsPropertiesFile)
        openbis1.setDataStoreServerUsername('etlserver1')
        openbis1.setDataStoreServerProperty("host-address", "https://localhost")
        openbis1.allUp()
 
        '''uncomment the following if we have not run the test once in non-dev mode before (otherwise we already have ENTITY_REGISTRATION and get an error'''
#        '''Drop the folder to register some test entities in space SYNC'''
#        openbis1.dropAndWait("ENTITY_REGISTRATION", "openbis-sync-entity-reg")

        openbis2 = self.createOpenbisController(instanceName = 'openbis2', port = openbis2_port, dropDatabases=False)
        openbis2.setDataStoreServerPort(openbis2_dss_port)
        openbis2.setOpenbisPortDataStoreServer(openbis2_port)
        openbis2.setDataStoreServerProperty("host-address", "https://localhost")
        self.installHarvesterPlugin(openbis2)
        openbis2.setDummyAuthentication()
        
        '''datasource plugin is installed on harvester side as well just for testing'''
        self.installDataSourcePlugin(openbis2, openbis2_dss_port)
        openbis2.setDataStoreServerUsername('etlserver2')
                
        #=======================================================================
        source = self.getHarvesterConfigFolder()
        
        destination = openbis2.installPath
        harvester_config_file_name= "harvester-config.txt"
        harvester_config_file_path = os.path.join(source, harvester_config_file_name)
        harvester_config_lines =  self.readLinesFromFile(harvester_config_file_path)

        data_source_alias = self.extractDataSourceAlias(harvester_config_lines)
        util.printAndFlush("Data source alias is: %s" % data_source_alias)

        util.printAndFlush("Copying harvester configuration file from %s to %s"% (source, destination))
        util.copyFromTo(source, destination, harvester_config_file_name)
        
        '''first do a dry run'''
        output = self.dry_run(openbis2_dss_port, openbis2, harvester_config_file_name, harvester_config_lines)

        '''then do a real run'''
        self.realRun(openbis1, openbis2_dss_port, openbis2, source, harvester_config_file_name, data_source_alias)

    def getEntityGraph(self, openbis2_dss_port, openbis_instance, user, password):
        harvester_graph_response = self.getResourceListForComparison(openbis2_dss_port, user, password)
        file = os.path.join(openbis_instance.installPath, "%s_graph.txt" % openbis_instance.instanceName)
        self.writeResponseToFile(harvester_graph_response, file)
        graph_lines = self.readLinesFromFile(file)
        return graph_lines

    def setDryRunToTrue(self, harvester_config_file_path, harvester_config_lines):
        with open(harvester_config_file_path, 'wb') as output:
            for line in harvester_config_lines:
                if line.startswith("dry-run") == True:
                    output.write("dry-run = true")
                else:
                    output.write(line)

    def dry_run(self, openbis2_dss_port, openbis2, harvester_config_file_name, harvester_config_lines):
        util.printAndFlush("Setting dry-run = true in modifying harvester-config.txt file")
        self.setDryRunToTrue(os.path.join(openbis2.installPath, harvester_config_file_name), harvester_config_lines)
        
        '''disable harvester maintenance task'''
        harvester_plugin_folder = os.path.join(openbis2.installPath, 'servers/core-plugins/openbis2/1/dss/maintenance-tasks/harvester')
        util.printAndFlush("Disabling harvester plugin until the initial entity graph is read")
        open(os.path.join(harvester_plugin_folder, 'disabled'), 'a')
        openbis2.allUp()
        '''read entity graph from harvester before starting dry run by removing disabled file from plugin folder'''
        harvester_graph_lines_before_dry_run = self.getEntityGraph(openbis2_dss_port, openbis2, 'testuser1', '123')
        openbis2.dssDown()
        util.printAndFlush("Enabling harvester plugin for the dry run")
        os.remove(os.path.join(harvester_plugin_folder, 'disabled'))
        openbis2.dssUp()
        monitor = util.LogMonitor("%s synchronization.log" % openbis2.instanceName, 
            "%s/synchronization.log" % openbis2.installPath) # "%s/servers/datastore_server/log/datastore_server_log.txt" % openbis2.installPath
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.HarvesterMaintenanceTask'))
        monitor.waitUntilEvent(util.RegexCondition('OPERATION.HarvesterMaintenanceTask - Dry run finished'))
        '''read entity graph from harvester after dry run finished'''
        harvester_graph_lines_after_dry_run = self.getEntityGraph(openbis2_dss_port, openbis2, 'testuser1', '123')
        harvester_graph_lines_before_dry_run.sort()
        harvester_graph_lines_after_dry_run.sort()
        diff_for_dry_run = self.diff(set(harvester_graph_lines_before_dry_run), set(harvester_graph_lines_after_dry_run))
        self.assertEquals("The entity graph on harvester stays the same after a dry run", True, len(diff_for_dry_run) == 0)

    def realRun(self, openbis1, openbis2_dss_port, openbis2, source, harvester_config_file_name, data_source_alias):
        destination = openbis2.installPath
        openbis2.dssDown()
        util.printAndFlush("Copying harvester configuration file from %s to %s" % (source, destination))
        util.copyFromTo(source, destination, harvester_config_file_name)
        openbis2.dssUp()
        '''read entity graph from datasource'''
        content1 = self.getEntityGraph('8444', openbis1, 'harvester1', '123')
        content1.sort()
        '''read entity graph from harvester
        the entities might be translated using a prefix specified in the harvester_config
        remove the prefix'''
        monitor = util.LogMonitor("%s synchronization.log" % openbis2.instanceName, 
            "%s/synchronization.log" % openbis2.installPath) # "%s/servers/datastore_server/log/datastore_server_log.txt" % openbis2.installPath
        monitor.addNotificationCondition(util.RegexCondition('OPERATION.HarvesterMaintenanceTask'))
        monitor.waitUntilEvent(util.RegexCondition('OPERATION.HarvesterMaintenanceTask - Saving the timestamp of sync start to file'))
        graph_lines = self.getEntityGraph(openbis2_dss_port, openbis2, 'testuser1', '123')
        content2 = self.removePrefixFromLines(graph_lines, data_source_alias)
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

    def extractDryRunProperty(self, lines):
        for line in lines:
            match = re.search("dry-run=\s(.*)", line)
            if match:
                val = match.group(1)
                print "val:" + val 
                if(val.lower() == "true") :
                    return True
                elif (val.lower() == "false"):
                    return False
                else:
                    self.fail("Dry-run value can either be true or false")
        return False
            
            
    def extractDataSourceAlias(self, lines):
        for line in lines:
            match = re.search("data-source-alias =\s(.*)", line)
            if match:
                return match.group(1)
        return ""
                
    def readLinesFromFile(self, input_file):
        with open(input_file, 'rb') as f:
            content = f.readlines()
        return content
    
    def removePrefixFromLines(self, lines, prefix):
        if prefix == "".strip():
            return
        temp = []
        for line in lines:
            temp.append(re.sub(prefix+"_", '', line))
        return temp

            
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
        project = 'sis/OpenbisSync'
        path = repository.getPathToArtifact(project, 'archive.zip')
        util.printAndFlush("downloaded repository as : %s" % path)
        destination = "%s/servers/core-plugins/%s/" % (openbisController.installPath, openbisController.instanceName)
        commit_id = repository.getLatestCommitHash(project)
        path_in_archive = "OpenbisSync-master-%s" % commit_id
        util.printAndFlush("path to plugin in repository repository as : %s" % path_in_archive)
        util.printAndFlush("Unzipping plugin % s into folder %s"% (plugin_name, destination))
        util.unzipSubfolder(path, path_in_archive + "/core-plugins/%s/1/" % plugin_name, destination)

    def installDataSourcePlugin(self, openbisController, dss_port):
        self.installPlugin(openbisController, "datasource")
        '''update datasource service plugin.properties'''
        datasource_core_plugin_properties = "%s/1/dss/services/resource-sync/plugin.properties" % openbisController.instanceName
        plugin_properties_file = os.path.join(openbisController.installPath, "servers", "core-plugins", datasource_core_plugin_properties)
        util.printAndFlush("Updating %s" % plugin_properties_file)
        pluginProps = util.readProperties(plugin_properties_file)
        pluginProps['request-handler.server-url'] = "https://localhost:%s/openbis" % dss_port
        pluginProps['request-handler.download-url'] = "https://localhost:%s" % dss_port
        util.writeProperties(plugin_properties_file, pluginProps)
        
        '''update db source plugin.properties'''
        datasource_core_plugin_properties = "%s/1/dss/data-sources/openbis-db/plugin.properties" % openbisController.instanceName
        plugin_properties_file = os.path.join(openbisController.installPath, "servers", "core-plugins", datasource_core_plugin_properties)
        util.printAndFlush("Updating db source %s" % plugin_properties_file)
        pluginProps = util.readProperties(plugin_properties_file)
        pluginProps['databaseKind'] = openbisController.databaseKind
        util.writeProperties(plugin_properties_file, pluginProps)
        
    def installHarvesterPlugin(self, openbisController):
        self.installPlugin(openbisController, "harvester")

    def getHarvesterConfigFolder(self):
        return systemtest.testcase.TEMPLATES + "/" + self.name + "/harvester_config"

    def getMasterDataScriptFolder(self):
        return systemtest.testcase.TEMPLATES + "/" + self.name + "/master_data"
    
class GitLabArtifactRepository(GitArtifactRepository):
    """
    Artifact repository for a gitlab projects.
    """
    def __init__(self, localRepositoryFolder, host = 'sissource.ethz.ch'):
        GitArtifactRepository.__init__(self, localRepositoryFolder)
        self.host = host

    def downloadArtifact(self, project, pattern):
        url = "https://%s/%s/repository/%s" % (self.host, project, pattern)
        util.printAndFlush("Download %s to %s." % (url, self.localRepositoryFolder))
        request = Request(url, headers = {'PRIVATE-TOKEN' : self._read_private_token()})
        self._download(urllib2.urlopen(request), pattern)
        return pattern
    
    def getLatestCommitHash(self, project):
        url = "https://%s/api/v4/projects/%s/repository/commits/master" % (self.host, project.replace('/','%2F'))
        request = Request(url, headers = {'PRIVATE-TOKEN' : self._read_private_token()})
        response = urllib2.urlopen(request)
        result = json.load(response)
        return result["id"]
    
    def _read_private_token(self):
        with open('targets/sissource_private-token.txt', 'r') as f:
            return f.readline().strip()

TestCase(settings, __file__).runTest()