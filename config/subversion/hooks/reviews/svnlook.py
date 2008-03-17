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
        command += " -t"
      else:
        command += " -r "
      command += self.revisionOrTransaction
    command += " " + str(self.repository)
    if args != None:
      command += " " + args
    proc = Popen4(command)
    proc.wait()
    return proc.fromchild.read()


  def getChangedFiles(self):
    """Lists all supported files that were changed in this revision"""
    output = self._svnlook("changed")
    output = output.split("\n");
    result = []
    reg = re.compile("\s+");    
    for line in output:
      if len(line) > 0 and \
          line[len(line)-1] != '/':
        l = reg.split(line)
        result.append(l[1])
    return self._filterUnsupportedFiles(result)
  
  def readFileForVersion(self, file, version=None):
    """Returns content of a given file for a particular revision (or the standard revision)"""
    if version:
      return self._svnlook("cat", file, "-r " + str(version))
    else:
      return self._svnlook("cat", file)

  def _filterUnsupportedFiles(self, files):
    sup_files = []
    supported_files =  \
        re.compile('^\S*(html|xml|php|js|css|py|txt|c|cc|cpp|ini|cf|conf|java|jsp|tmpl|readme|properties)$')
    for file in files:
      if supported_files.match(file.lower()):
        sup_files.append(file)
    return sup_files
