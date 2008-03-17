#!/usr/bin/python
import re

def generateSnippet(text, length=45):
  """Takes a text and returns snippet for it

  From a given text returns a snippet consisting of
  a few first words, no longer than length +1 word
  """
  words = text.split()
  snippet = ""
  i = 0
  while (len(snippet) < length and
      i < len(words)):
    snippet += words[i] + " "
    i += 1

  snippet = snippet.rstrip()
  if len(snippet) < len(text):
    snippet += "..."

  return snippet

def filterUnsupportedFiles(names):
  return names
