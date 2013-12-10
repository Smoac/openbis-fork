import re
import os
import os.path
import shutil
import time
import traceback

import util

INSTALLER_PROJECT = 'gradle-installation'
OPENBIS_STANDARD_TECHNOLOGIES_PROJECT = 'gradle-openbis-standard-technologies'
DATAMOVER_PROJECT = 'datamover'

PLAYGROUND = 'targets/playground'
TEMPLATES = 'templates'
TEST_DATA = 'testData'
PSQL_EXE = 'psql'

class TestCase(object):
    """
    Abstract superclass of a test case. 
    Subclasses have to override execute() and optionally executeInDevMode(). 
    The test case is run by invoking runTest(). 
    Here is a skeleton of a test case:
    
    #!/usr/bin/python
    import settings
    import systemtest.testcase

    class TestCase(systemtest.testcase.TestCase):
    
        def execute(self):
            ....
            
        def executeInDevMode(self):
            ....
            
    TestCase(settings, __file__).runTest()

    There are two execution modes (controlled by command line option -d and -rd):
    
    Normal mode: 
        1. Cleans up playground: Kills running servers and deletes playground folder of this test case.
        2. Invokes execute() method.
        3. Release resources: Shuts down running servers.
        
    Developing mode:
        1. Invokes executeInDevMode() method.
        
    The developing mode allows to reuse already installed servers. 
    Servers might be restarted. This mode leads to fast development 
    of test code by doing incremental development. Working code
    can be moved from executeInDevMode() to execute(). 
    """
    def __init__(self, settings, filePath):
        self.artifactRepository = settings.REPOSITORY
        self.project = None
        fileName = os.path.basename(filePath)
        self.name = fileName[0:fileName.rfind('.')]
        self.playgroundFolder = "%s/%s" % (PLAYGROUND, self.name)
        self.numberOfFailures = 0
        self.devMode = settings.devMode
        self.runningInstances = []

    def runTest(self):
        """
        Runs this test case. This is a final method. It should not be overwritten.
        """
        startTime = time.time()
        print "\n/''''''''''''''''''' %s started at %s %s ''''''''''" % (self.name, time.strftime('%Y-%m-%d %H:%M:%S'),
                                                                         'in DEV MODE' if self.devMode else '')
        try:
            if not self.devMode:
                if os.path.exists(self.playgroundFolder):
                    self._cleanUpPlayground()
                os.makedirs(self.playgroundFolder)
                self.execute()
            else:
                self.executeInDevMode()
            success = self.numberOfFailures == 0
        except:
            traceback.print_exc()
            success = False
            raise Exception("%s failed" % self.name)
        finally:
            duration = time.time() - startTime
            if not self.devMode:
                self.releaseResources()
            if success:
                print "\...........SUCCESS: %s executed in %d seconds .........." % (self.name, duration)
            else:
                print "\............FAILED: %s executed in %d seconds .........." % (self.name, duration)
        
    def execute(self):
        """
        Executes this test case in normal mode. 
        This is an abstract method which has to be overwritten in subclasses.
        """
        pass
    
    def executeInDevMode(self):
        """
        Executes this test case in developing mode. 
        This method can be overwritten in subclasses.
        """
        pass
    
    def releaseResources(self):
        """
        Releases resources. It shuts down all running servers.
        This method can be overwritten in subclasses. 
        Note, this method can be invoked in subclasses as follows:
        
                super(type(self), self).releaseResources()
        
        """
        self._shutdownSevers()
        
    def assertPatternInLog(self, log, pattern):
        if not re.search(pattern, log):
            self.fail("Pattern doesn't match: %s" % pattern)
    
    def assertEquals(self, itemName, expected, actual):
        """
        Asserts that expected == actual. If not the test will be continued but counted as failed.
        """
        if expected != actual:
            self.fail("%s\n  expected: <%s>\n   but was: <%s>" % (itemName, expected, actual))
        else:
            print "%s as expected: <%s>" % (itemName, expected)
    
    def fail(self, errorMessage):
        """
        Prints specified error message and mark test case as failed.
        """
        self.numberOfFailures += 1
        print "ERROR: %s" % errorMessage
        
    def installScriptBasedServer(self, templateName, instanceName, 
                                 startCommand = ['./start.sh'], stopCommand = ['./stop.sh']):
        installPath = self._getInstallPath(instanceName)
        if os.path.exists(installPath):
            shutil.rmtree(installPath)
        shutil.copytree("%s/%s" % (self.getTemplatesFolder(), templateName), installPath)
        return ScriptBasedServerController(self, self.name, installPath, instanceName, startCommand, stopCommand)
        
    def createScriptBasedServerController(self, instanceName, startCommand = ['./start.sh'], stopCommand = ['./stop.sh']):
        return ScriptBasedServerController(self, self.name, self._getInstallPath(instanceName), instanceName, 
                                           startCommand, stopCommand)
        
    def installDatamover(self, instanceName = 'datamover'):
        zipFile = self.artifactRepository.getPathToArtifact(DATAMOVER_PROJECT, 'datamover')
        installPath = self._getInstallPath(instanceName)
        util.unzip(zipFile, self.playgroundFolder)
        os.rename("%s/datamover" % (self.playgroundFolder), installPath)
        return DatamoverController(self, self.name, installPath, instanceName)
    
    def createDatamoverController(self, instanceName = 'datamover'):
        return DatamoverController(self, self.name, self._getInstallPath(instanceName), instanceName)

    def installOpenbis(self, instanceName = 'openbis', technologies = []):
        """
        Installs openBIS from the installer. 
        
        The instanceName specifies the subfolder in the playground folder 
        where the instance will be installed. 
        In addition it is also part of the database names.
        The technologies are an array of enabled technologies.
        """
        installerPath = self.artifactRepository.getPathToArtifact(INSTALLER_PROJECT, 'openBIS-installation')
        installerFileName = os.path.basename(installerPath).split('.')[0]
        util.executeCommand(['tar', '-zxf', installerPath, '-C', self.playgroundFolder], 
                            "Couldn't untar openBIS installer.")
        consolePropertiesFile = "%s/%s/console.properties" % (self.playgroundFolder, installerFileName)
        consoleProperties = util.readProperties(consolePropertiesFile)
        installPath = self._getInstallPath(instanceName)
        consoleProperties['INSTALL_PATH'] = installPath
        consoleProperties['DSS_ROOT_DIR'] = "%s/data" % installPath
        for technology in technologies:
            consoleProperties[technology.upper()] = True
        util.writeProperties(consolePropertiesFile, consoleProperties)
        util.executeCommand("%s/%s/run-console.sh" % (self.playgroundFolder, installerFileName), 
                            "Couldn't install openBIS", consoleInput='admin\nadmin')
        shutil.rmtree("%s/%s" % (self.playgroundFolder, installerFileName))
        
    def cloneOpenbisInstance(self, nameOfInstanceToBeCloned, nameOfNewInstance, dataStoreServerOnly = False):
        """ Clones an openBIS instance. """
        
        oldInstanceInstallPath = "%s/%s" % (self.playgroundFolder, nameOfInstanceToBeCloned)
        newInstanceInstallPath = "%s/%s" % (self.playgroundFolder, nameOfNewInstance)
        paths = ['bin', 'data', 'servers/core-plugins', 'servers/datastore_server']
        if not dataStoreServerOnly:
            paths.append('servers/openBIS-server')
        for path in paths:
            util.copyFromTo(oldInstanceInstallPath, newInstanceInstallPath, path)
        dssPropsFile = "%s/servers/datastore_server/etc/service.properties" % newInstanceInstallPath
        dssProps = util.readProperties(dssPropsFile)
        dssProps['root-dir'] = dssProps['root-dir'].replace(nameOfInstanceToBeCloned, nameOfNewInstance)
        util.writeProperties(dssPropsFile, dssProps)
        
    
    def createOpenbisController(self, instanceName = 'openbis', dropDatabases = True):
        """
        Creates an openBIS controller object assuming that an openBIS instance for the specified name is installed.
        """
        return OpenbisController(self, self.name, self._getInstallPath(instanceName), instanceName, dropDatabases)
    
    def installScreeningTestClient(self):
        """ Installs the screening test client and returns an instance of ScreeningTestClient. """
        zipFile = self.artifactRepository.getPathToArtifact(OPENBIS_STANDARD_TECHNOLOGIES_PROJECT, 'openBIS-screening-API')
        installPath = "%s/screeningAPI" % self.playgroundFolder
        util.unzip(zipFile, installPath)
        return ScreeningTestClient(self, installPath)
    
    def getTemplatesFolder(self):
        return "%s/%s" % (TEMPLATES, self.name)
    
    def _getInstallPath(self, instanceName):
        return os.path.abspath("%s/%s" % (self.playgroundFolder, instanceName))

    def _cleanUpPlayground(self):
        for f in os.listdir(self.playgroundFolder):
            path = "%s/%s" % (self.playgroundFolder, f)
            if not os.path.isdir(path):
                continue
            print "clean up %s" % path
            util.killProcess("%s/servers/datastore_server/datastore_server.pid" % path)
            util.killProcess("%s/servers/openBIS-server/jetty/openbis.pid" % path)
            util.killProcess("%s/datamover.pid" % path)
        util.deleteFolder(self.playgroundFolder)

    def _getAndCreateFolder(self, folderPath):
        """
        Creates a folder inside the playground. The argument is a relative path to the playground.
        The returned path is relative to the working directory.
        """
        path = "%s/%s" % (self.playgroundFolder, folderPath)
        os.makedirs(path)
        return path
    
    def _addToRunningInstances(self, controller):
        self.runningInstances.append(controller)
        
    def _removeFromRunningInstances(self, controller):
        if controller in self.runningInstances:
            self.runningInstances.remove(controller)
    
    def _shutdownSevers(self):
        for instance in reversed(self.runningInstances):
            instance.stop()
            
class _Controller(object):
    def __init__(self, testCase, testName, installPath, instanceName):
        self.testCase = testCase
        self.testName = testName
        self.instanceName = instanceName
        self.installPath = installPath
        print "Controller created for instance '%s'. Installation path: %s" % (instanceName, installPath)


    def assertEmptyFolder(self, pathRelativeToInstallPath):
        """
        Asserts that the specified path (relative to the installation path) is an empty folder.
        """
        relativePath = "%s/%s" % (self.installPath, pathRelativeToInstallPath)
        files = self._getFiles(relativePath)
        if len(files) == 0:
            print "Empty folder as expected: %s" % relativePath
        else:
            self.testCase.fail("%s isn't empty. It contains the following files:\n  %s" % (relativePath, files))
            
    def assertFiles(self, folderPathRelativeToInstallPath, expectedFiles):
        """
        Asserts that the specified path (relative to the installation path) contains the specified files.
        """
        relativePath = "%s/%s" % (self.installPath, folderPathRelativeToInstallPath)
        files = self._getFiles(relativePath)
        self.testCase.assertEquals("Files in %s" % relativePath, expectedFiles, sorted(files))
        
    def _getFiles(self, relativePath):
        if not os.path.isdir(relativePath):
            self.testCase.fail("Doesn't exist or isn't a folder: %s" % relativePath)
        files = os.listdir(relativePath)
        return files

            
class ScriptBasedServerController(_Controller):
    def __init__(self, testCase, testName, installPath, instanceName, startCommand, stopCommand):
        super(ScriptBasedServerController, self).__init__(testCase, testName, installPath, instanceName)
        self.startCommand = startCommand
        self.stopCommand = stopCommand
        
    def start(self):
        self.testCase._addToRunningInstances(self)
        util.executeCommand(self.startCommand, "Couldn't start server '%s'" % self.instanceName, 
                            workingDir = self.installPath)
        
    def stop(self):
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand(self.stopCommand, "Couldn't stop server '%s'" % self.instanceName, 
                            workingDir = self.installPath)
        
            
class DatamoverController(_Controller):
    def __init__(self, testCase, testName, installPath, instanceName):
        super(DatamoverController, self).__init__(testCase, testName, installPath, instanceName)
        self.servicePropertiesFile = "%s/etc/service.properties" % self.installPath
        self.serviceProperties = util.readProperties(self.servicePropertiesFile)
        self.serviceProperties['check-interval'] = 2
        self.serviceProperties['quiet-period'] = 5
        self.serviceProperties['inactivity-period'] = 15
        dataCompletedScript = "%s/%s/data-completed.sh" % (testCase.getTemplatesFolder(), instanceName)
        if os.path.exists(dataCompletedScript):
            self.serviceProperties['data-completed-script'] = "../../../../%s" % dataCompletedScript
        
    def setPrefixForIncoming(self, prefix):
        """ Set service property 'prefix-for-incoming'. """
        self.serviceProperties['prefix-for-incoming'] = prefix
        
    def setTreatIncomingAsRemote(self, flag):
        """ Set service property 'treat-incoming-as-remote'. """
        self.serviceProperties['treat-incoming-as-remote'] = flag
        
    def setOutgoingTarget(self, path):
        """ 
        Set service property 'outgoing-target'. 
        This has to be a path relative to installation path of the datamover. 
        """
        self.serviceProperties['outgoing-target'] = path
        
    def setExtraCopyDir(self, path):
        """ 
        Set service property 'extra-copy-dir'. 
        This has to be a path relative to installation path of the datamover. 
        """
        self.serviceProperties['extra-copy-dir'] = path
        
    def start(self):
        """ Starts up datamover server. """
        util.writeProperties(self.servicePropertiesFile, self.serviceProperties)
        self.testCase._addToRunningInstances(self)
        output = util.executeCommand(["%s/datamover.sh" % self.installPath, 'start'], suppressStdOut=True)
        joinedOutput = '\n'.join(output)
        if 'FAILED' in joinedOutput:
            print "Start up of datamover %s failed:\n%s" % (self.instanceName, joinedOutput)
            raise Exception("Couldn't start up datamover '%s'." % self.instanceName)
            
    def stop(self):
        """ Stops datamover server. """
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand(["%s/datamover.sh" % self.installPath, 'stop'],
                            "Couldn't shut down datamover '%s'." % self.instanceName)
        
    def drop(self, testDataSetName):
        """ Drops the specified test data set into incoming folder. """
        util.copyFromTo(TEST_DATA, "%s/data/incoming" % self.installPath, testDataSetName)

            
class ScreeningTestClient(object):
    """
    Class representing the screeing test client.
    """
    def __init__(self, testCase, installPath):
        self.testCase = testCase
        self.installPath = installPath
        
    def run(self):
        """ Runs the test client and returns the console output as a list of strings. """
        output = util.executeCommand(['java', '-Djavax.net.ssl.trustStore=openBIS.keystore',
                                      '-jar', 'openbis_screening_api.jar', 'admin', 'admin', 
                                      'https://localhost:8443'], suppressStdOut = True,
                                      workingDir=self.installPath)
        with open("%s/log.txt" % self.installPath, 'w') as log:
            for line in output:
                log.write("%s\n" % line)
        return output
    
class DataSet(object):
        def __init__(self, resultSetRow):
            self.id = resultSetRow[0]
            self.dataStore = resultSetRow[1]
            self.experimentCode = resultSetRow[2]
            self.code = resultSetRow[3]
            self.type = resultSetRow[4]
            self.location = resultSetRow[5]
            self.producer = resultSetRow[6]
            self.productionTimeStamp = resultSetRow[7]
            self.parents = []
            self.children = []
        
        def __str__(self):
            parents = [d.id for d in self.parents]  
            children = [d.id for d in self.children]
            return "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s" % (self.id, self.dataStore, self.code, self.type, 
                                                      self.location, parents, children, self.experimentCode, 
                                                      self.producer, self.productionTimeStamp) 
    
class OpenbisController(_Controller):
    """
    Class to control AS and DSS of an installed openBIS instance.
    """
    def __init__(self, testCase, testName, installPath, instanceName, dropDatabases = True):
        """
        Creates a new instance for specifies test case with specified test and instance name, installation path.
        """
        super(OpenbisController, self).__init__(testCase, testName, installPath, instanceName)
        self.templatesFolder = testCase.getTemplatesFolder()
        self.binFolder = "%s/bin" % installPath
        self.bisUpScript = "%s/bisup.sh" % self.binFolder
        self.bisDownScript = "%s/bisdown.sh" % self.binFolder
        self.dssUpScript = "%s/dssup.sh" % self.binFolder
        self.dssDownScript = "%s/dssdown.sh" % self.binFolder
        self.databaseKind = "%s_%s" % (testName, instanceName)
        self.asServicePropertiesFile = "%s/servers/openBIS-server/jetty/etc/service.properties" % installPath
        self.asProperties = None
        if os.path.exists(self.asServicePropertiesFile):
            self.asProperties = util.readProperties(self.asServicePropertiesFile)
            self.asProperties['database.kind'] = self.databaseKind
            self.asPropertiesModified = True
        self.dssServicePropertiesFile = "%s/servers/datastore_server/etc/service.properties" % installPath
        self.dssProperties = util.readProperties(self.dssServicePropertiesFile)
        self.dssProperties['path-info-db.databaseKind'] = self.databaseKind
        self.dssProperties['imaging-database.kind'] = self.databaseKind
        self.dssProperties['proteomics-database-kind'] = self.databaseKind
        self.dssPropertiesModified = True
        if dropDatabases:
            util.dropDatabase(PSQL_EXE, "openbis_%s" % self.databaseKind)
            util.dropDatabase(PSQL_EXE, "pathinfo_%s" % self.databaseKind)
            util.dropDatabase(PSQL_EXE, "imaging_%s" % self.databaseKind)
            util.dropDatabase(PSQL_EXE, "proteomics_%s" % self.databaseKind)
        self._applyCorePlugins()
        
    def setDummyAuthentication(self):
        """ Disables authentication. """
        self.asProperties['authentication-service'] = 'dummy-authentication-service'
        
    def setDataStoreServerCode(self, code):
        """ Sets the code of the Data Store Server. """
        self.dssProperties['data-store-server-code'] = code
        
    def getDataStoreServerCode(self):
        return self.dssProperties['data-store-server-code']
        
    def setDataStoreServerPort(self, port):
        """ Sets the port of the Data Store Server. """
        self.dssProperties['port'] = port
        
    def setDataStoreServerUsername(self, username):
        """ Sets the username of the Data Store Server. """
        self.dssProperties['username'] = username
        
    def assertFileExist(self, pathRelativeToInstallPath):
        """
        Asserts that the specified path (relative to the installation path) exists.
        """
        relativePath = "%s/%s" % (self.installPath, pathRelativeToInstallPath)
        if os.path.exists(relativePath):
            print "Path exists as expected: %s" % relativePath
        else:
            self.testCase.fail("Path doesn't exist: %s" % relativePath)
            
    def assertDataSetContent(self, pathToOriginal, dataSet):
        path = "%s/data/store/1/%s/original" % (self.installPath, dataSet.location)
        path = "%s/%s" % (path, os.listdir(path)[0])
        numberOfDifferences = util.getNumberOfDifferences(pathToOriginal, path)
        if numberOfDifferences > 0:
            self.testCase.fail("%s differences found." % numberOfDifferences)
            
    def assertNumberOfDataSets(self, expectedNumberOfDataSets, dataSets):
        """
        Asserts that the specified number of data sets from the specified list of DataSet instances 
        are in the data store. 
        """
        count = 0
        for dataSet in dataSets:
            if dataSet.dataStore != self.getDataStoreServerCode():
                continue
            count += 1
            self.assertFileExist("data/store/1/%s/original" % dataSet.location)
        self.testCase.assertEquals("Number of data sets in data store %s" % self.getDataStoreServerCode(), 
                                   expectedNumberOfDataSets, count)
    
    def getDataSets(self):
        """
        Returns all data sets as a list (ordered by data set ids) of instances of class DataSet.
        """
        resultSet = self.queryDatabase('openbis', 
                                       "select data.id,ds.code,e.code,data.code,t.code,location,"
                                       + "    data.data_producer_code,data.production_timestamp from data"
                                       + " join external_data as ed on ed.data_id = data.id" 
                                       + " join data_set_types as t on data.dsty_id = t.id"
                                       + " join experiments as e on data.expe_id = e.id"
                                       + " join data_stores as ds on data.dast_id = ds.id order by data.id")
        dataSets = []
        dataSetsById = {}
        for row in resultSet:
            dataSet = DataSet(row)
            dataSets.append(dataSet)
            dataSetsById[dataSet.id] = dataSet
        relationships = self.queryDatabase('openbis', 
                                           "select data_id_parent, data_id_child from data_set_relationships"
                                           + " order by data_id_parent, data_id_child")
        for parent_id, child_id in relationships:
            parent = dataSetsById[parent_id]
            child = dataSetsById[child_id]
            parent.children.append(child)
            child.parents.append(parent)
        print "All data sets:\nid,dataStore,code,type,location,experiment,parents,children\n"
        for dataSet in dataSets:
            print dataSet
        return dataSets 
        
    def createTestDatabase(self, databaseType):
        """
        Creates a test database for the specified database type.
        """
        database = "%s_%s" % (databaseType, self.databaseKind)
        scriptPath = "%s/%s.sql" % (self.templatesFolder, database)
        util.createDatabase(PSQL_EXE, database, scriptPath)
        
    def queryDatabase(self, databaseType, queryStatement):
        """
        Executes the specified SQL statement for the specified database type. Result set is returned
        as a list of lists.
        """
        database = "%s_%s" % (databaseType, self.databaseKind)
        return util.queryDatabase(PSQL_EXE, database, queryStatement)
    
    def allUp(self):
        """ Starts up AS and DSS. """
        self._saveAsPropertiesIfModified()
        self._saveDssPropertiesIfModified()
        self.testCase._addToRunningInstances(self)
        util.executeCommand([self.bisUpScript], "Starting up openBIS AS '%s' failed." % self.instanceName)
        util.executeCommand([self.dssUpScript], "Starting up openBIS DSS '%s' failed." % self.instanceName)
        
    def stop(self):
        self.allDown()
        
    def allDown(self):
        """ Shuts down AS and DSS. """
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand([self.dssDownScript], "Shutting down openBIS DSS '%s' failed." % self.instanceName)
        if self.asProperties:
            util.executeCommand([self.bisDownScript], "Shutting down openBIS AS '%s' failed." % self.instanceName)
        
    def dssUp(self):
        """ Starts up DSS. """
        self._saveDssPropertiesIfModified()
        self.testCase._addToRunningInstances(self)
        util.executeCommand([self.dssUpScript], "Starting up openBIS DSS '%s' failed." % self.instanceName)
        
    def dssDown(self):
        """ Shuts down DSS. """
        self.testCase._removeFromRunningInstances(self)
        util.executeCommand([self.dssDownScript], "Shutting down openBIS DSS '%s' failed." % self.instanceName)
        
    def drop(self, zipFileName, dropBoxName, numberOfDataSets = 1, timeOutInMinutes = 5):
        """
        Unzip the specified ZIP file into the specified drop box and wait until data set has been registered.
        """
        util.unzip("%s/%s" % (self.templatesFolder, zipFileName), "%s/data/%s" % (self.installPath, dropBoxName))
        self.waitUntilDataSetRegistrationFinished(numberOfDataSets = numberOfDataSets, timeOutInMinutes = timeOutInMinutes)
        
    def waitUntilDataSetRegistrationFinished(self, numberOfDataSets = 1, timeOutInMinutes = 5):
        """ Waits until the specified number of data sets have been registrated. """
        monitor = util.LogMonitor("%s.DSS" % self.instanceName, 
                                  "%s/servers/datastore_server/log/datastore_server_log.txt" % self.installPath, 
                                  timeOutInMinutes)
        monitor.addNotificationCondition(util.RegexCondition('Incoming Data Monitor'))
        monitor.addNotificationCondition(util.RegexCondition('post-registration'))
        numberOfRegisteredDataSets = 0
        while numberOfRegisteredDataSets < numberOfDataSets:
            elements = monitor.waitUntilEvent(util.RegexCondition('Post registration of (\\d*). of \\1 data sets'))
            numberOfRegisteredDataSets += int(elements[0])
            print "%d of %d data sets registered" % (numberOfRegisteredDataSets, numberOfDataSets)
        
    def waitUntilDataSetRegistrationFailed(self, timeOutInMinutes = 5):
        """ Waits until data set registration failed. """
        monitor = util.LogMonitor("%s.DSS" % self.instanceName, 
                                  "%s/servers/datastore_server/log/datastore_server_log.txt" % self.installPath, 
                                  timeOutInMinutes)
        monitor.addNotificationCondition(util.RegexCondition('Incoming Data Monitor'))
        monitor.addNotificationCondition(util.RegexCondition('post-registration'))
        monitor.waitUntilEvent(util.EventTypeCondition('ERROR'))
        print "Data set registration failed as expected."
        
    def _applyCorePlugins(self):
        corePluginsFolder = "%s/servers/core-plugins" % self.installPath
        destination = "%s/%s" % (corePluginsFolder, self.instanceName)
        shutil.rmtree(destination, ignore_errors=True)
        shutil.copytree("%s/core-plugins/%s" % (self.templatesFolder, self.instanceName), destination)
        corePluginsPropertiesFile = "%s/core-plugins.properties" % corePluginsFolder
        corePluginsProperties = util.readProperties(corePluginsPropertiesFile)
        enabledModules = corePluginsProperties['enabled-modules']
        enabledModules = "%s, %s" % (enabledModules, self.instanceName) if len(enabledModules) > 0 else self.instanceName
        corePluginsProperties['enabled-modules'] = enabledModules
        util.writeProperties(corePluginsPropertiesFile, corePluginsProperties)
        
    def _saveAsPropertiesIfModified(self):
        if self.asPropertiesModified:
            util.writeProperties(self.asServicePropertiesFile, self.asProperties)
            self.asPropertiesModified = False
    
    def _saveDssPropertiesIfModified(self):
        if self.dssPropertiesModified:
            util.writeProperties(self.dssServicePropertiesFile, self.dssProperties)
            self.dssPropertiesModified = False
    
