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

class TestCase():
    def __init__(self, settings, filePath):
        self.artifactRepository = settings.REPOSITORY
        self.project = None
        fileName = os.path.basename(filePath)
        self.name = fileName[0:fileName.rfind('.')]
        self.playgroundFolder = "%s/%s" % (PLAYGROUND, self.name)
        self.numberOfFailures = 0
        self.suppressCleanUp = settings.suppressCleanUp

    def runTest(self):
        """
        Runs this test case. This is a final method. It should not be overwritten.
        """
        startTime = time.time()
        print "\n/''''''''''''''''''' %s started at %s ''''''''''" % (self.name, time.strftime('%Y-%m-%d %H:%M:%S'))
        try:
            if os.path.exists(self.playgroundFolder):
                if not self.suppressCleanUp:
                    self._cleanUpPlayground()
                    os.makedirs(self.playgroundFolder)
            else:
                os.makedirs(self.playgroundFolder)
            self.execute()
            success = self.numberOfFailures == 0
        except:
            traceback.print_exc()
            success = False
            raise Exception("%s failed" % self.name)
        finally:
            duration = time.time() - startTime
            if success:
                print "\...........SUCCESS: %s executed in %d seconds .........." % (self.name, duration)
            else:
                print "\............FAILED: %s executed in %d seconds .........." % (self.name, duration)
        
    def execute(self):
        """
        Executes this test case. This is an abstract method which has to be overwritten in subclasses.
        """
        pass
    
    def assertEquals(self, itemName, expected, actual):
        """
        Asserts that expected == actual. If not both will be printed and the test will be counted as failed.
        """
        if expected != actual:
            self.numberOfFailures += 1
            print "ERROR: %s\n  expected: <%s>\n   but was: <%s>" % (itemName, expected, actual)
        else:
            print "%s as expected: <%s>" % (itemName, expected)
    

    def installOpenbis(self, instanceName = 'openbis', technologies = []):
        """
        Installs openBIS from the installer. 
        
        The instanceName specifies the subfolder in the playground folder where the instance will be installed. 
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
        return OpenbisController(self.name, installPath, instanceName)
    
    def createOpenbisController(self, instanceName = 'openbis'):
        return OpenbisController(self.name, self._getOpenbisInstallPath(instanceName), instanceName, dropDatabases = False)
    
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
    
class OpenbisController():
    """
    Class to control AS and DSS of an installed openBIS instance.
    """
    def __init__(self, testName, installPath, instanceName, dropDatabases = True):
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
        if dropDatabases:
            util.dropDatabase(PSQL_EXE, "openbis_%s" % self.databaseKind)
        self.dssServicePropertiesFile = "%s/servers/datastore_server/etc/service.properties" % installPath
        self.dssProperties = util.readProperties(self.dssServicePropertiesFile)
        self.dssProperties['path-info-db.databaseKind'] = self.databaseKind
        self.dssProperties['imaging-database.kind'] = self.databaseKind
        self.dssPropertiesModified = True
        if dropDatabases:
            util.dropDatabase(PSQL_EXE, "pathinfo_%s" % self.databaseKind)
        self._applyCorePlugins()
        
    def createTestDatabase(self, databaseType):
        database = "%s_%s" % (databaseType, self.databaseKind)
        scriptPath = "%s/%s.sql" % (self.templatesFolder, database)
        util.createDatabase(PSQL_EXE, database, scriptPath)
        
    def queryDatabase(self, databaseType, queryStatement):
        database = "%s_%s" % (databaseType, self.databaseKind)
        return util.queryDatabase(PSQL_EXE, database, queryStatement)
        
    def allUp(self):
        """
        Starts up AS and DSS.
        """
        self._saveAsPropertiesIfModified()
        self._saveDssPropertiesIfModified()
        util.executeCommand([self.bisUpScript], "Starting up openBIS AS '%s' failed." % self.instanceName)
        util.executeCommand([self.dssUpScript], "Starting up openBIS DSS '%s' failed." % self.instanceName)
        
    def allDown(self):
        """
        Shuts down AS and DSS.
        """
        self._saveAsPropertiesIfModified()
        self._saveDssPropertiesIfModified()
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
        monitor.waitUntilEvent(util.RegexCondition('Post registration'))
        
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
    
