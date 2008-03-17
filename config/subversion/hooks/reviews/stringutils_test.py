#!/usr/bin/python
import unittest
import stringutils

class StringUtilsTest(unittest.TestCase):
  """Tests behaviour of stringutils module"""
  def testGenerateSnippetShortString(self):
    string1 = "a short string"
    self.assertEquals(string1, stringutils.generateSnippet(string1))
  
  def testGenerateSnippetShortString(self):
    string1 = "a very very very very very very very very very very very " + \
        "very very very long text"
    string2 = "a very very very very very very..."
    self.assertEquals(string2, stringutils.generateSnippet(string1))


if __name__ == "__main__":
  unittest.main() 
