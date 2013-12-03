import re
import os
import os.path
import shutil
import time
import traceback

import util

INSTALLER_PROJECT = 'gradle-installation'
OPENBIS_STANDARD_TECHNOLOGIES_PROJECT = 'gradle-openbis-standard-technologies'

PLAYGROUND = 'targets/playground'
TEMPLATES = 'templates'
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
        self.runningOpenbisInstances = []

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
        

    def installOpenbis(self, instanceName = 'openbis', technologies = []):
        """
        Installs openBIS from the installer. 
        
        The instanceName specifies the subfolder in the playground folder 
        where the instance will be installed. 
        In addition it is also part of the database names.
        The technologies are an array of enabled technologies.
        An instance of OpenbisController is returned.
        """
        installerPath = self.artifactRepository.getPathToArtifact(INSTALLER_PROJECT, 'openBIS-installation')
        installerFileName = os.path.basename(installerPath).split('.')[0]
        util.executeCommand(['tar', '-zxf', installerPath, '-C', self.playgroundFolder], 
                            "Couldn't untar openBIS installer.")
        consolePropertiesFile = "%s/%s/console.properties" % (self.playgroundFolder, installerFileName)
        consoleProperties = util.readProperties(consolePropertiesFile)
        installPath = self._getOpenbisInstallPath(instanceName)
        consoleProperties['INSTALL_PATH'] = installPath
        consoleProperties['DSS_ROOT_DIR'] = "%s/data" % installPath
        for technology in technologies:
            consoleProperties[technology.upper()] = True
        util.writeProperties(consolePropertiesFile, consoleProperties)
        util.executeCommand("%s/%s/run-console.sh" % (self.playgroundFolder, installerFileName), 
                            "Couldn't install openBIS", consoleInput='admin\nadmin')
        return OpenbisController(self, self.name, installPath, instanceName)
    
    def createOpenbisController(self, instanceName = 'openbis'):
        """
        Creates an openBIS controller object assuming that an openBIS instance for the specified name is installed.
        """
        return OpenbisController(self, self.name, self._getOpenbisInstallPath(instanceName), instanceName, dropDatabases = False)
    
    def installScreeningTestClient(self):
        """ Installs the screening test client and returns an instance of ScreeningTestClient. """
        zipFile = self.artifactRepository.getPathToArtifact(OPENBIS_STANDARD_TECHNOLOGIES_PROJECT, 'openBIS-screening-API')
        installPath = "%s/screeningAPI" % self.playgroundFolder
        util.unzip(zipFile, installPath)
        return ScreeningTestClient(self, installPath)
    
    def _getOpenbisInstallPath(self, instanceName):
        return os.path.abspath("%s/%s" % (self.playgroundFolder, instanceName))

    def _cleanUpPlayground(self):
        for f in os.listdir(self.playgroundFolder):
            path = "%s/%s" % (self.playgroundFolder, f)
            if not os.path.isdir(path):
                continue
            print "clean up %s" % path
            util.killProcess("%s/servers/datastore_server/datastore_server.pid" % path)
            util.killProcess("%s/servers/openBIS-server/jetty/openbis.pid" % path)
        util.deleteFolder(self.playgroundFolder)

    def _getAndCreateFolder(self, folderPath):
        """
        Creates a folder inside the playground. The argument is a relative path to the playground.
        The returned path is relative to the working directory.
        """
        path = "%s/%s" % (self.playgroundFolder, folderPath)
        os.makedirs(path)
        return path
    
    def _shutdownSevers(self):
        for instance in self.runningOpenbisInstances:
            instance.allDown()
            
class ScreeningTestClient():
    """
    Class representing the screeing test client.
    """
    def __init__(self, testCase, installPath):
        self.testCase = testCase
        self.installPath = installPath
        
    def run(self):
        """ Runs the test client and returns the console output as a list of strings. """
        currentDir = os.getcwd()
        os.chdir(self.installPath)
        try:
            output = util.executeCommand(['java', '-Djavax.net.ssl.trustStore=openBIS.keystore',
                                          '-jar', 'openbis_screening_api.jar', 'admin', 'admin', 
                                          'https://localhost:8443'], suppressStdOut = True)
        finally:
            os.chdir(currentDir)
        with open("%s/log.txt" % self.installPath, 'w') as log:
            for line in output:
                log.write("%s\n" % line)
        return output
    
class OpenbisController():
    """
    Class to control AS and DSS of an installed openBIS instance.
    """
    def __init__(self, testCase, testName, installPath, instanceName, dropDatabases = True):
        """
        Creates a new instance for specifies test case with specified test and instance name, installation path.
        """
        self.testCase = testCase
        self.testName = testName
        self.instanceName = instanceName
        self.installPath = installPath
        self.templatesFolder = "%s/%s" % (TEMPLATES, testName)
        self.binFolder = "%s/bin" % installPath
        self.bisUpScript = "%s/bisup.sh" % self.binFolder
        self.bisDownScript = "%s/bisdown.sh" % self.binFolder
        self.dssUpScript = "%s/dssup.sh" % self.binFolder
        self.dssDownScript = "%s/dssdown.sh" % self.binFolder
        self.asServicePropertiesFile = "%s/servers/openBIS-server/jetty/etc/service.properties" % installPath
        self.asProperties = util.readProperties(self.asServicePropertiesFile)
        self.databaseKind = "%s_%s" % (testName, instanceName)
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
        
    def assertFileExist(self, pathRelativeToInstallPath):
        """
        Asserts that the specified path (relative to the installation path) exists.
        """
        relativePath = "%s/%s" % (self.installPath, pathRelativeToInstallPath)
        if os.path.exists(relativePath):
            print "Path exists as expected: %s" % relativePath
        else:
            self.testCase.fail("Path doesn't exist: %s" % relativePath)
            
    def assertEmptyFolder(self, pathRelativeToInstallPath):
        """
        Asserts that the specified path (relative to the installation path) is an empty folder.
        """
        relativePath = "%s/%s" % (self.installPath, pathRelativeToInstallPath)
        if not os.path.isdir(relativePath):
            self.testCase.fail("Doesn't exist or isn't a folder: %s" % relativePath)
        files = os.listdir(relativePath)
        if len(files) == 0:
            print "Empty folder as expected: %s" % relativePath
        else:
            self.testCase.fail("%s isn't empty. It contains the following files:\n  %s" % (relativePath, files))
            
    def assertNumberOfDataSets(self, expectedNumberOfDataSets):
        """
        Asserts that the specified number of data sets are in the store and in the database.
        Some meta data of all data sets are returned.
        """
        metaData = self.queryDatabase("openbis", "select e.code,data.code,t.code,location from data"
                                               + " join external_data as ed on ed.data_id = data.id" 
                                               + " join data_set_types as t on data.dsty_id = t.id"
                                               + " join experiments as e on data.expe_id = e.id")
        util.printResultSet(metaData)
        self.testCase.assertEquals('number of data sets', expectedNumberOfDataSets, len(metaData));
        for row in metaData:
            self.assertFileExist("data/store/1/%s/original" % row[3])
        return metaData
        
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
        """
        Starts up AS and DSS.
        """
        self._saveAsPropertiesIfModified()
        self._saveDssPropertiesIfModified()
        self.testCase.runningOpenbisInstances.append(self)
        util.executeCommand([self.bisUpScript], "Starting up openBIS AS '%s' failed." % self.instanceName)
        util.executeCommand([self.dssUpScript], "Starting up openBIS DSS '%s' failed." % self.instanceName)
        
    def allDown(self):
        """
        Shuts down AS and DSS.
        """
        self._saveAsPropertiesIfModified()
        self._saveDssPropertiesIfModified()
        self.testCase.runningOpenbisInstances.remove(self)
        util.executeCommand([self.dssDownScript], "Shutting down openBIS DSS '%s' failed." % self.instanceName)
        util.executeCommand([self.bisDownScript], "Shutting down openBIS AS '%s' failed." % self.instanceName)
        
    def drop(self, zipFileName, dropBoxName, timeOutInMinutes = 1):
        """
        Unzip the specified ZIP file into the specified drop box and wait until data set has been registered.
        """
        util.unzip("%s/%s" % (self.templatesFolder, zipFileName), "%s/data/%s" % (self.installPath, dropBoxName))
        monitor = util.LogMonitor("%s.DSS" % self.instanceName, 
                                  "%s/servers/datastore_server/log/datastore_server_log.txt" % self.installPath, 
                                  timeOutInMinutes=5)
        monitor.addNotificationCondition(util.RegexCondition('Incoming Data Monitor'))
        monitor.waitUntilEvent(util.RegexCondition('Post registration of (\\d*). of \\1 data sets'))
        
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
    
