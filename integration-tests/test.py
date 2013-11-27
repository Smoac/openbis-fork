#!/usr/bin/python
"""
Runs all test cases in alphabetical order. A test case is a file of type '.py' and starts with 'test-'.
Exit value will 0 if all test cases succeeded otherwise it will be 1.
"""
import os
import os.path
import sys
import time

import settings

startTime = time.time() 
numberOfTestCases = 0
numberOfFailedTestCases = 0
for f in sorted(os.listdir(os.path.dirname(__file__))):
    splittedFileName = f.rsplit('.', 1)
    if len(splittedFileName) > 1:
        moduleName = splittedFileName[0]
        fileType = splittedFileName[1]
        if moduleName.startswith('test_') and fileType == 'py':
            numberOfTestCases += 1
            try:
                __import__(moduleName)
            except:
                numberOfFailedTestCases += 1
print '====================================='
print "%d test cases executed in %d seconds" % (numberOfTestCases, time.time() - startTime)
if numberOfFailedTestCases == 0:
    print "no test case failed"
    exit(0)
if numberOfFailedTestCases == 1:
    print "1 test case failed"
else:
    print "%d test cases failed" % numberOfFailedTestCases
exit(1)
