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


  def getDiffForFile(self, file):
    """Returns html diff for a given file, comparing last and 
       previous revisions"""
    new = self.svnlook.readFileForVersion(file, self.revision)
    old = self.svnlook.readFileForVersion(file, self.revision - 1)
    diff = HtmlDiff(2, self.line_length)
    table = diff.make_table(old.split('\n'), new.split('\n'));
    return table

 
  def getDiffsForAllFiles(self):
    """Returns html diff tables for all files present in a given revision"""
    files = self.svnlook.getChangedFiles()
    result = ""
    for file in files:
      result += "\n<h2>" + file + "</h2>\n";
      result += self.getDiffForFile(file)
    return result

  
  def getAuthor(self):
    """Returns author of the revision"""
    return self.svnlook._svnlook("author").replace("\n",'')


  def getDescription(self):
    """Returns description of the revision (more than just svnlook info!)"""
    description = self.svnlook._svnlook("info")
    description += "\n\n" + self.svnlook._svnlook("changed")
    return description


  def getDescriptionSnippet(self):
    """Returns snippet of the description of a revision"""
    info = self.svnlook._svnlook("info")
    lines = info.split("\n")
    description = "%d: " % self.revision
    description += " ".join(lines[3:])
    return stringutils.generateSnippet(description, length=self.subject_length)
    
  
  def getTextForAllFiles(self):
    """Returns content of all files in revision"""
    files = self.getChangedFiles()
    result = ""
    for file in files:
      result += self._formatTextHeader(file)
      result += self._enumerateLines(self.readFileForVersion(file, self.revision))
    return result


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

