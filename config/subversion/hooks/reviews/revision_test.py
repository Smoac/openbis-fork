#!/usr/bin/python
import unittest
import revision

class RevisionTest(unittest.TestCase):
  """Tests behaviour of revision module"""
  def testSupportedFilesEmptyList(self):
    rev = revision.Revision("",1)
    self.assertEquals([],rev._filterUnsupportedFiles([]))

  def testSupportedFilterAllUnsupportedFiles(self):
    rev = revision.Revision("",1)
    test = ["g.jpg", "f.bin", "a.tar.gz", "b.tar.bz2", "c.dpkg", \
      "d.cue", "h.JPG", "f.exe", "f.rpm", "d.iso", "FOO"]
    self.assertEquals([],rev._filterUnsupportedFiles(test))
  
  def testSupportedFilterAllUnsupportedFiles(self):
    rev = revision.Revision("",1)
    test = ["g.txt", "f.html", "a.css", "b.java", "c.py", \
      "d.html", "h.cpp", "f.cc", "f.c", "d.cf", "f.tmpl", \
      "s.txt", "README"]
    self.assertEquals(test, rev._filterUnsupportedFiles(test))

if __name__ == "__main__":
  unittest.main() 
