#!/usr/bin/python

import sys
import os
import re
import stringutils
import syslog
from difflib_2_5 import HtmlDiff
from svnlook import SvnLook


class Revision:
  """Wraps subversion revision into an object"""

  def __init__(self, repository, revision, line_length = 80, subject_length = 100):
    self.repository = repository
    self.revision = int(revision)
    self.line_length = line_length
    self.subject_length = subject_length
    self.svnlook = SvnLook(repository, revision)
    self.changed_files = None
    self.info = None
    self.author = None


  def getDiffForFile(self, change, file):
    """Returns html diff for a given file, comparing last and 
       previous revisions"""
    if change == 'A': 
      new = self.svnlook.readFileForVersion(file, self.revision)
      old = "NEW"
    elif change == 'D':
      new = "DELETED"
      old = self.svnlook.readFileForVersion(file, self.revision - 1)
    else:
      new = self.svnlook.readFileForVersion(file, self.revision)
      old = self.svnlook.readFileForVersion(file, self.revision - 1)
    diff = HtmlDiff(2, self.line_length)
    table = diff.make_table(old.split('\n'), new.split('\n'));
    return table

 
  def getDiffsForAllFiles(self):
    """Returns html diff tables for all files present in a given revision"""
    files = self.getChangedFiles()
    result = ""
    for (change, file) in files:
      result += "\n<h2>" + file + "</h2>\n";
      result += self.getDiffForFile(change, file)
    return result


  def getChangedFiles(self):
    return self._filterUnsupportedFiles(self.svnlook.processChangedFiles(_getRawChangedFiles()))


  def getAuthor(self):
    """Returns author of the revision"""
    if not self.author:
      self.author = self.svnlook._svnlook("author").replace("\n",'')
    return self.author


  def getDescription(self):
    """Returns description of the revision (more than just svnlook info!)"""
    description = self._getSvnInfo()
    description += "\n\n" + self._getRawChangedFiles()
    return description


  def getDescriptionSnippet(self):
    """Returns snippet of the description of a revision"""
    info = self._getSvnInfo()
    lines = info.split("\n")
    description = "%d: " % self.revision
    description += " ".join(lines[3:])
    return stringutils.generateSnippet(description, length=self.subject_length)
    
  
  def getTextForAllFiles(self):
    """Returns content of all files in revision"""
    files = self.getChangedFiles()
    result = ""
    for (change, file) in files:
      result += self._formatTextHeader(file)
      if change != 'D':
        result += self._enumerateLines(self.readFileForVersion(file, self.revision))
      else:
        result += "<strong>DELETED</strong>"
    return result


  def _getRawChangedFiles(self):
    if not self.changed_files:
      self.changed_files = self.svnlook.getRawChangedFiles()
    return self.changed_files

  
  def _getSvnInfo(self):
    if not self.info:
      self.info = self.svnlook._svnlook("info")
    return self.info
  

  def _formatTextHeader(self, file):
    return "\n\n******  " + file + "  ******\n\n"

  
  def _enumerateLines(self, text):
    lines = text.split("\n")
    line_num = 1
    result = ""
    for line in lines:
      result += ("%4d  " % line_num) + line + "\n"
      line_num += 1
    return result

  def _filterUnsupportedFiles(self, files):
    sup_files = []
    supported_files =  \
        re.compile('^\S*(html|xml|php|js|css|py|txt|c|cc|cpp|ini|cf|conf|java||sql|jsp|tmpl|readme|properties)$')
    for (change, file) in files:
      if supported_files.match(file.lower()):
        sup_files.append((change, file))
    return sup_files
