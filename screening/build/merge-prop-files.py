#!/usr/bin/python
"""
  Usage:
    ./merge-prop-files.py <source.properties> <target.properties>
  
  Merges the contents of the <source> property file into <target> overriding existing 
  entries. If a property from <source> does not exist in <target> it is appended at the end. 
"""

import sys
import re


def readPropFile(srcFileName):
    dict = {}
    f = open(srcFileName)
    
    for line in f:
        line = line.strip()
        if (len(line) == 0) or line.startswith("#"):
            pass
        else:
            (key, delim, value) = line.partition('=')
            dict[key.strip()] = value.strip()

    f.close()        
    return dict


def replaceProperty(key, value, fileContent):
    newLine = "\n" + key + "=" + value + "\n"
    replacedContent = re.sub(r"\s+" + key + "\s*=.*\n", newLine, fileContent)
    if replacedContent == fileContent: 
        # no match found, appending property at the end of the file
        replacedContent = replacedContent + newLine
        
    return replacedContent


def mergePropFiles(srcFileName, targetFileName):
    props = readPropFile(srcFileName)
    
    readHandle = open(targetFileName)
    targetFileContent = readHandle.read();
    readHandle.close();
    
    for (key, value) in props.items():
        targetFileContent = replaceProperty(key, value, targetFileContent)
    
    writeHandle = open(targetFileName, "w")
    writeHandle.write(targetFileContent);
    writeHandle.close();


if __name__ == "__main__":
    srcFileName = sys.argv[1]
    targetFileName = sys.argv[2]
    mergePropFiles(srcFileName, targetFileName)
    #mergePropFiles('/Users/kaloyane/cisd/modules/screening/dist/installer/dss-service.properties', 
    #               '/Users/kaloyane/cisd/modules/screening/dist/installer/test-dss-service.properties')
