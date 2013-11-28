import os
import os.path
import shutil
import subprocess
import time
import traceback

import util

INSTALLER_PROJECT = 'gradle-installation'
OPENBIS_STANDARD_TECHNOLOGIES_PROJECT = 'gradle-openbis-standard-technologies'

PLAYGROUND = 'targets/playground'
PSQL_EXE = 'psql'

class TestCase():
    def __init__(self, artifactRepository, filePath):
        self.artifactRepository = artifactRepository
        self.project = None
        fileName = os.path.basename(filePath)
        self.name = fileName[0:fileName.rfind('.')]
        self.playgroundFolder = "%s/%s" % (PLAYGROUND, self.name)

    def runTest(self):
        """
        Runs this test case. This is a final method. It should not be overwritten.
        """
        startTime = time.time()
        print "\n/''''''''''''''''''' %s started at %s ''''''''''" % (self.name, time.strftime('%Y-%m-%d %H:%M:%S'))
        try:
            if os.path.exists(self.playgroundFolder):
                self._cleanUpPlayground()
            os.makedirs(self.playgroundFolder)
            self.execute()
            success = True
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
    
    def installOpenbis(self, instanceName = 'openbis_test', technologies = []):
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
        installPath = os.path.abspath("%s/%s" % (self.playgroundFolder, instanceName))
        consoleProperties['INSTALL_PATH'] = installPath
        consoleProperties['DSS_ROOT_DIR'] = "%s/data" % installPath
        for technology in technologies:
            consoleProperties[technology.upper()] = True
        util.writeProperties(consolePropertiesFile, consoleProperties)
        util.executeCommand("%s/%s/run-console.sh" % (self.playgroundFolder, installerFileName), 
                            "Couldn't install openBIS", consoleInput='admin\nadmin')
        return OpenbisController(installPath, instanceName)
    
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
    def __init__(self, installPath, instanceName):
        self.instanceName = instanceName
        self.installPath = installPath
        self.binFolder = "%s/bin" % installPath
        self.bisUpScript = "%s/bisup.sh" % self.binFolder
        self.bisDownScript = "%s/bisdown.sh" % self.binFolder
        self.dssUpScript = "%s/dssup.sh" % self.binFolder
        self.dssDownScript = "%s/dssdown.sh" % self.binFolder
        self.asServicePropertiesFile = "%s/servers/openBIS-server/jetty/etc/service.properties" % installPath
        self.asProperties = util.readProperties(self.asServicePropertiesFile)
        self.asProperties['database.kind'] = instanceName
        self.asPropertiesModified = True
        util.dropDatabase(PSQL_EXE, "openbis_%s" % instanceName)
        self.dssServicePropertiesFile = "%s/servers/datastore_server/etc/service.properties" % installPath
        self.dssProperties = util.readProperties(self.dssServicePropertiesFile)
        self.dssProperties['path-info-db.databaseKind'] = instanceName
        self.dssPropertiesModified = True
        util.dropDatabase(PSQL_EXE, "pathinfo_%s" % instanceName)
        
    def createTestDatabase(self, databaseKind, scriptPath):
        util.createDatabase(PSQL_EXE, "%s_%s" % (databaseKind, self.instanceName), scriptPath)
        
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
        
    def _saveAsPropertiesIfModified(self):
        if self.asPropertiesModified:
            util.writeProperties(self.asServicePropertiesFile, self.asProperties)
            self.asPropertiesModified = False
    
    def _saveDssPropertiesIfModified(self):
        if self.dssPropertiesModified:
            util.writeProperties(self.dssServicePropertiesFile, self.dssProperties)
            self.dssPropertiesModified = False
    
