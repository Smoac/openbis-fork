import re
import os

from java.lang import Exception
from java.io import File

# =================================
#  Generic utility functions
# =================================

""" 
Finds first occurence of the patter from the right.
Throws exception if the pattern cannot be found.
"""
def rfind(text, pattern):
    ix = text.rfind(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

""" 
Finds first occurence of the patter from the left. 
Throws exception if the pattern cannot be found.
"""
def find(text, pattern):
    ix = text.find(pattern)
    ensurePatternFound(ix, text, pattern)
    return ix

def ensurePatternFound(ix, file, pattern):
    if ix == -1:
        raise Exception("Cannot find '" + pattern + "' pattern in file name '" + file + "'")    

""" Returns: name of the file without the extension """
def extractFileBasename(filename):
    lastDot = filename.rfind(".")
    if lastDot != -1:
        return filename[0:lastDot]
    else:
        return filename

""" Returns: extension of the file """
def getFileExt(file):
    return os.path.splitext(file)[1][1:].lower()

""" Returns: java.io.File - first file with the specified extension or None if no file matches """
def findFileByExt(incomingFile, expectedExt):
    if not incomingFile.isDirectory():
        return None
    incomingPath = incomingFile.getPath()
    for file in os.listdir(incomingPath):
        ext = getFileExt(file)
        if ext.upper() == expectedExt.upper():
            return File(incomingPath, file)
    return None

""" Returns: java.io.File - subdirectory which contains the specified marker in the name """
def findDir(incomingFile, dirNameMarker):
    if not incomingFile.isDirectory():
        return None
    incomingPath = incomingFile.getPath()
    for file in os.listdir(incomingPath):
        if dirNameMarker.upper() in file.upper():
            return File(incomingPath, file)
    return None

""" Removes trailing empty strings from a list """
def removeTrailingEmptyElements(list):
    pos = len(list)
    while (pos > 0):
        pos = pos - 1
        if not list[pos].strip():
            del list[pos]
        else:
            break
    return list

