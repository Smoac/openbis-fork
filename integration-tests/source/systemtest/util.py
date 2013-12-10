import filecmp
import os
import os.path
import re
import time
import shutil
import subprocess

USER=os.environ['USER']

def readProperties(propertiesFile):
    """
    Reads a Java properties file and returns the key-value pairs as a dictionary.
    """
    with open(propertiesFile, "r") as f:
        result = {}
        for line in f.readlines():
            trimmedLine = line.strip()
            if len(trimmedLine) > 0 and not trimmedLine.startswith('#'):
                splittedLine = line.split('=', 1)
                key = splittedLine[0].strip()
                value = splittedLine[1].strip()
                result[key] = value
        return result
    
def writeProperties(propertiesFile, dictionary):
    """
    Saves the specified dictionary as a Java properties file.
    """
    with open(propertiesFile, "w") as f:
        for key in sorted(dictionary):
            f.write("%s=%s\n" % (key, dictionary[key]))
            
def executeCommand(commandWithArguments, failingMessage = None, consoleInput = None, suppressStdOut = False, 
                   workingDir = None):
    """
    Executes specified command with arguments. 
    If the exit value of the command is not zero and a failing message has been specified 
    an exception with the failing message will be thrown. 
    Optionally a string for console input can be specified.
    If flag suppressStdOut is set standard output will be suppressed but returned as a list of output lines.
    If workingDir is specified a change to workingDir is done for execution.
    """
    print "\n------- START: %s" % commandWithArguments
    currentDir = None
    if workingDir != None:
        print "change to working directory '%s'" % workingDir
        currentDir = os.getcwd()
        os.chdir(workingDir)
    try:
        processIn = subprocess.PIPE if consoleInput != None else None
        processOut = subprocess.PIPE if suppressStdOut else None
        p = subprocess.Popen(commandWithArguments, stdin = processIn, stdout = processOut)
        if consoleInput != None:
            p.communicate(consoleInput)
        lines = []
        if suppressStdOut:
            for line in iter(p.stdout.readline,''):
                lines.append(line.strip())
        exitValue = p.wait()
        if currentDir != None:
            print "change back to previous working directory '%s'" % currentDir
        if exitValue != 0 and failingMessage != None: 
            print "---- FAILED %d: %s" % (exitValue, commandWithArguments)
            raise Exception(failingMessage)
        print "---- FINISHED: %s" % commandWithArguments
        return lines
    finally:
        if currentDir != None:
            os.chdir(currentDir)
        
    
def killProcess(pidFile):
    """
    Kills the process in specified PID file. Does nothing if PID file doesn't exist.
    """
    if not os.path.exists(pidFile):
        return
    with open(pidFile, 'r') as handle:
        pid = handle.readline().rstrip()
        executeCommand(['kill', pid])
        
def unzip(zipFile, destination):
    """
    Unzips specified ZIP file at specified destination.
    """
    executeCommand(['unzip', '-q', '-o', zipFile, '-d', destination], "Couldn't unzip %s at %s" % (zipFile, destination))
    
def deleteFolder(folderPath):
    """
    Deletes the specified folder.
    Raises an exception in case of error.
    """
    print "Delete %s" % folderPath
    def errorHandler(*args):
        _, path, _ = args
        raise Exception("Couldn't delete %s" % path)
    shutil.rmtree(folderPath, onerror = errorHandler)
    
def copyFromTo(sourceFolder, destinationFolder, relativePathInSourceFolder):
    shutil.copytree("%s/%s" % (sourceFolder, relativePathInSourceFolder), 
                    "%s/%s" % (destinationFolder, relativePathInSourceFolder))
    print "%s copied from %s to %s" % (relativePathInSourceFolder, sourceFolder, destinationFolder)
    
def dropDatabase(psqlExe, database):
    """
    Drops the specified database by using the specified path to psql.
    """
    executeCommand([psqlExe, '-U', 'postgres', '-c' , "drop database if exists %s" % database], 
                   "Couldn't drop database %s" % database)
    
def createDatabase(psqlExe, database, scriptPath = None):
    """
    Creates specified database and run (if defined) the specified SQL script. 
    """
    executeCommand([psqlExe, '-U', 'postgres', '-c' , "create database %s with owner %s" % (database, USER)], 
                   "Couldn't create database %s" % database)
    if scriptPath == None:
        return
    executeCommand([psqlExe, '-q', '-U', USER, '-d', database,  '-f', scriptPath], suppressStdOut=True,
                   failingMessage="Couldn't execute script %s for database %s" % (scriptPath, database))
    
def queryDatabase(psqlExe, database, queryStatement):
    """
    Queries specified database by applying specified SQL statement and returns the result set as a list
    where each row is a list, too.
    """
    lines = executeCommand([psqlExe, '-U', 'postgres', '-tA', '-d', database, '-c', queryStatement], 
                           "Couldn't execute query: %s" % queryStatement, suppressStdOut = True)
    result = []
    for line in lines:
        result.append(line.split('|'))
    return result

def printResultSet(resultSet):
    """
    Prints the specified result set.
    """
    for row in resultSet:
        print row

def getNumberOfDifferences(fileOrFolder1, fileOrFolder2):
    """
    Gets and reports differences in file system structures between both arguments.
    """
    result = filecmp.dircmp(fileOrFolder1, fileOrFolder2, ignore=['.svn'])
    result.report()
    return len(result.left_only) + len(result.right_only) + len(result.diff_files)

def getContent(path):
    with open(path, "r") as f:
        return [ l.rstrip() for l in f.readlines()]
    
class LogMonitor():
    """
    Monitor of a log file. Conditions can be specified for printing a notification and waiting. 
    
    A condition has to be a class with method 'match' which has two string arguments: 
    Event type and log message. It returns 'None' in case of no match and 
    a tuple with zero or more matching elements found in log message.
    """
    def __init__(self, logName, logFilePath, timeOutInMinutes = 5):
        """
        Creates an instance with specified log name (used in notification), log file, and time out.
        """
        self.logName = logName
        self.logFilePath = logFilePath
        self.timeOutInMinutes = timeOutInMinutes
        self.conditions = []
        self.timeProvider = time
        class SystemPrinter:
            def printMsg(self, msg):
                print msg
        self.printer = SystemPrinter()
    
    def addNotificationCondition(self, condition):
        """
        Adds a notification condition
        """
        self.conditions.append(condition)
        
    def waitUntilEvent(self, condition):
        """
        Waits until an event matches the specified condition. 
        Returns tuple with zero or more elements of matching log message.
        """
        self.conditions.append(condition)
        startTime = self.timeProvider.time()
        renderedStartTime = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(startTime))
        self.printer.printMsg("\n>>>>> Start monitoring %s log at %s >>>>>>>>>>>>>>>>>>>>" 
                              % (self.logName, renderedStartTime))
        finalTime = startTime + self.timeOutInMinutes * 60
        try:
            alreadyPrintedLines = set()
            while True:
                log = open(self.logFilePath, 'r')
                while True:
                    actualTime = self.timeProvider.time()
                    if actualTime > finalTime:
                        raise Exception("Time out after %d minutes for monitoring %s log." 
                                        % (self.timeOutInMinutes, self.logName))
                    line = log.readline()
                    if line == '':
                        break
                    match = re.match('(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}),\d{3} (.{6})(.*)', line)
                    if match == None:
                        continue
                    timestamp = match.group(1)
                    eventType = match.group(2).strip()
                    message = match.group(3)
                    eventTime = time.mktime(time.strptime(timestamp, '%Y-%m-%d %H:%M:%S'))
                    if eventTime < startTime:
                        continue
                    for c in self.conditions:
                        if c.match(eventType, message) != None and not line in alreadyPrintedLines:
                            alreadyPrintedLines.add(line)
                            self.printer.printMsg(">> %s" % line.strip())
                            break
                    elements = condition.match(eventType, message)
                    if elements != None:
                        return elements
                log.seek(0, os.SEEK_CUR)
                time.sleep(5)
        finally:
            self.printer.printMsg(">>>>> Finished monitoring %s log >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" 
                                  % self.logName)
            
class EventTypeCondition():
    """ A condition which matches in case of specified event type. """
    def __init__(self, eventType):
        self.eventType = eventType
        
    def match(self, eventType, message):
        return () if self.eventType == eventType else None
        
class StartsWithCondition():
    """
    A condition which matches if the message starts with a specified string.
    """
    def __init__(self, startsWithString):
        self.startsWithString = startsWithString
        
    def match(self, eventType, message):
        return () if message.startswith(self.startsWithString) else None
    
class RegexCondition():
    """
    A condition which matches if the message matches a specified regular expression.
    """
    def __init__(self, regex):
        self.regex = regex
        
    def match(self, eventType, message):
        match = re.search(self.regex, message)
        return match.groups() if match else None
