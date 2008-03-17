#!/usr/bin/python

import os
import re

from popen2 import Popen4

class SvnLook:
  """Wraps calls to svnlook into an object"""
  def __init__(self, repository, revisionOrTransaction, isTransaction=False):
    self.repository = repository
    self.revisionOrTransaction = revisionOrTransaction
    self.isTransaction = isTransaction
    self._status = 0

  def _svnlook(self, command, args=None, revarg=None):
    """Handles all communication with svn repository

    executes "svnlook command" and returns its output
    takes self.revision as a standard revision and 
    self.repository as standard repository location
    """
    command = "svnlook " + command
    if revarg != None:
      command += " " + revarg
    else:
      if self.isTransaction:
        command += " -t "
      else:
        command += " -r "
      command += self.revisionOrTransaction
    command += " " + str(self.repository)
    if args != None:
      command += " " + args
    proc = Popen4(command)
    output = proc.fromchild.read()
    self._status = proc.wait()
    if self._status != 0:
      output = "Process '%s' exited with an error condition, output was:\n%s" % (command, output)
    return output


  def getStatus(self):
    return self._status


  def getRawChangedFiles(self):
    """Returns the raw output of 'svnlook changed'."""
    return self._svnlook("changed")


  def processChangedFiles(self, rawChangedFiles):
    """Produces a list of all files changed in this revision from the output of getRawChangedFiles()."""
    output = rawChangedFiles.split("\n");
    result = []
    reg = re.compile("\s+");    
    for line in output:
      if len(line) > 0 and \
          line[len(line)-1] != '/':
        l = reg.split(line)
        if len(l) == 2:
          result.append(l)
    return result
    

  def getChangedFiles(self):
    """Lists all supported files that were changed in this revision"""
    return self.processChangedFiles(self.getRawChangedFiles())

  
  def readFileForVersion(self, file, version=None):
    """Returns content of a given file for a particular revision (or the standard revision)"""
    if version:
      return self._svnlook("cat", file, "-r " + str(version))
    else:
      return self._svnlook("cat", file)

