#!/usr/bin/python

#
# Creates a list of 'mv' commands renaming files and directories containing _YF_ to '_HIV1_'.
#
# author: Izabela Adamczyk

import os
import shutil

currentDir = os.getcwd() + '/'

# Rename files
for (dir, subdirs, files) in os.walk('.'):
  for file in files:
    if file.find('_YF_') != -1:
      fileToBeChanged = currentDir + dir + '/' + file
      targetFilename = currentDir + dir + '/' + file.replace('_YF_', '_HIV1_')
      command = "mv %s %s" % (fileToBeChanged, targetFilename)
      print "echo", command
      print command
      # shutil.move(fileToBeChanged, targetFilename)
      print ""
print "echo Files: Done!"

# Rename directories
for (dir, subdirs, files) in os.walk('.'):
  for subdir in subdirs:
    if subdir.find('_YF_') != -1:
      dirToBeChanged = currentDir + dir + '/' + subdir
      targetFilename = currentDir + dir + '/' + subdir.replace('_YF_', '_HIV1_')
      command = "mv %s %s" % (dirToBeChanged, targetFilename)
      print "echo", command
      print command
      #shutil.move(dirToBeChanged, targetFilename)
      print ""
print "echo Directories: Done!"

print "echo Done!"
