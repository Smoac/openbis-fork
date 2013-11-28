import os
import os.path
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
            trimmedLine = line.lstrip().rstrip()
            if len(trimmedLine) > 0 and not trimmedLine.startswith('#'):
                splittedLine = line.split('=', 1)
                key = splittedLine[0].lstrip().rstrip()
                value = splittedLine[1].lstrip().rstrip()
                result[key] = value
        return result
    
def writeProperties(propertiesFile, dictionary):
    """
    Saves the specified dictionary as a Java properties file.
    """
    with open(propertiesFile, "w") as f:
        for key in sorted(dictionary):
            f.write("%s=%s\n" % (key, dictionary[key]))
            
def executeCommand(commandWithArguments, failingMessage = None, consoleInput = None, suppressStdOut = False):
    """
    Executes specified command with arguments. 
    If the exit value of the command is not zero and a failing message has been specified 
    an exception with the failing message will be thrown. 
    Optionally a string for console input can be specified.
    Standard output will be suppressed if flag suppressStdOut is set.
    """
    print "------- START: %s" % commandWithArguments
    processIn = subprocess.PIPE if consoleInput != None else None
    processOut = subprocess.PIPE if suppressStdOut else None
    p = subprocess.Popen(commandWithArguments, stdin = processIn, stdout = processOut)
    if consoleInput != None:
        p.communicate(consoleInput)
    exitValue = p.wait()
    if exitValue != 0 and failingMessage != None: 
        print "---- FAILED %d: %s" % (exitValue, commandWithArguments)
        raise Exception(failingMessage)
    print "---- FINISHED: %s" % commandWithArguments
    

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
    
def killProcess(pidFile):
    """
    Kills the process in specified PID file. Does nothing if PID file doesn't exist.
    """
    if not os.path.exists(pidFile):
        return
    with open(pidFile, 'r') as handle:
        pid = handle.readline().rstrip()
        executeCommand(['kill', pid], "Failed to kill process %s" % pid)
        
def dropDatabase(psqlExe, database):
    """
    Drops the specified database by using the specified path to psql.
    """
    executeCommand([psqlExe, '-U', 'postgres', '-c' , "drop database %s" % database])
    
def createDatabase(psqlExe, database, scriptPath = None):
    executeCommand([psqlExe, '-U', 'postgres', '-c' , "create database %s with owner %s" % (database, USER)], 
                   "Couldn't create database %s" % database)
    if scriptPath == None:
        return
    executeCommand([psqlExe, '-q', '-U', USER, '-d', database,  '-f', scriptPath], suppressStdOut=True,
                   failingMessage="Couldn't execute script %s for database %s" % (scriptPath, database))
