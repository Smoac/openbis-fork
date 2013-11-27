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

class TestCase():
    def __init__(self, artifactRepository, filePath):
        self.artifactRepository = artifactRepository
        self.project = None
        fileName = os.path.basename(filePath)
        self.name = fileName[0:fileName.rfind('.')]
        self.playgroundFolder = "%s/%s" % (PLAYGROUND, self.name)
        if os.path.exists(self.playgroundFolder):
            shutil.rmtree(self.playgroundFolder)
        os.makedirs(self.playgroundFolder)

    def runTest(self):
        """
        Runs this test case. This is a final method. It should not be overwritten.
        """
        startTime = time.time()
        print "\n/''''''''''''''''''' %s started at %s ''''''''''" % (self.name, time.strftime('%Y-%m-%d %H:%M:%S'))
        try:
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
    
    def installOpenbis(self, instanceName = 'openbis', technologies = []):
        """
        Installs openBIS from the installer. The instanceName specifies the subfolder in the playground folder
        where the instance will be installed. The technologies are an array of enabled technologies.
        An instance of OpenbisController is returned.
        """
        installerPath = self.artifactRepository.getPathToArtifact(INSTALLER_PROJECT, 'openBIS-installation')
        installerFileName = os.path.basename(installerPath).split('.')[0]
        exitValue = subprocess.call(['tar', '-zxf', installerPath, '-C', self.playgroundFolder])
        if exitValue > 0: raise Exception("Couldn't untar openBIS installer.")
        consolePropertiesFile = "%s/%s/console.properties" % (self.playgroundFolder, installerFileName)
        consoleProperties = util.readProperties(consolePropertiesFile)
        installPath = os.path.abspath("%s/%s" % (self.playgroundFolder, instanceName))
        consoleProperties['INSTALL_PATH'] = installPath
        consoleProperties['DSS_ROOT_DIR'] = "%s/data" % installPath
        for technology in technologies:
            consoleProperties[technology.upper()] = True
        util.writeProperties(consolePropertiesFile, consoleProperties)
        p = subprocess.Popen("%s/%s/run-console.sh" % (self.playgroundFolder, installerFileName), stdin = subprocess.PIPE)
        p.communicate('admin\nadmin')
        exitValue = p.wait()
        if exitValue > 0: raise Exception("Couldn't install openBIS.")
        return OpenbisController(installPath)
        
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
    def __init__(self, installPath):
        self.installPath = installPath
        self.asServicePropertiesFile = "%s/servers/openBIS-server/jetty/etc/service.properties" % installPath
        self.asProperties = util.readProperties(self.asServicePropertiesFile)
        self.dssServicePropertiesFile = "%s/servers/datastore_server/etc/service.properties" % installPath
        self.dssProperties = util.readProperties(self.dssServicePropertiesFile)
    
