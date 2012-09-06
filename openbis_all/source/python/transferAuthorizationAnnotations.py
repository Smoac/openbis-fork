#!/usr/bin/python

def readFile(path):
  lines = []
  with open(path, "r") as file:
    for line in file:
      lines += [line.rstrip()]
  return lines
  
def transferAnnotations(interfaceSourceFilePath, to):
  interfaceSourceCode = readFile(interfaceSourceFilePath)
  