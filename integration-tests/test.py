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
from systemtest.util import printAndFlush, renderDuration

startTime = time.time()
testCases = [] 
failedTestCases = {}
numberOfTestCases = 0
numberOfFailedTestCases = 0
for f in sorted(os.listdir(os.path.dirname(os.path.abspath(__file__)))):
    splittedFileName = f.rsplit('.', 1)
    if len(splittedFileName) > 1:
        moduleName = splittedFileName[0]
        fileType = splittedFileName[1]
        if moduleName.startswith('test_') and fileType == 'py':
            testCases.append(moduleName)
            try:
                __import__(moduleName)
            except:
                failedTestCases[moduleName] = sys.exc_info()
renderedStartTime = time.strftime('%Y-%m-%dT%H:%M:%S', time.localtime(startTime))
renderedDuration = renderDuration(time.time() - startTime)
testResultsFolder = 'targets/test-results'
if not os.path.exists(testResultsFolder):
    os.mkdir(testResultsFolder)
with open('targets/test-results/TEST-integration.xml', 'w') as out:
    out.write('<?xml version="1.1" encoding="UTF-8"?>\n') 
    out.write("<testsuite name='integration' tests='%s' failures='%s' errors='0' timestamp='%s' time='%s'>\n"
              % (len(testCases), len(failedTestCases), renderedStartTime, renderedDuration))
    for testCase in testCases:
        if testCase in failedTestCases:
            out.write("  <testcase name='%s'>\n" % testCase)
            out.write("    <failure>\n")
            out.write("      %s\n" % failedTestCases[testCase][1])
            out.write("    </failure>\n")
            out.write("  </testcase>\n")
        else:
            out.write("  <testcase name='%s'/>\n" % testCase)
    out.write("</testsuite>\n") 
printAndFlush('=====================================')
printAndFlush("%d test cases executed in %s" % (len(testCases), renderedDuration))
if numberOfFailedTestCases == 0:
    printAndFlush("no test case failed")
    exit(0)
if numberOfFailedTestCases == 1:
    printAndFlush("1 test case failed")
else:
    printAndFlush("%d test cases failed" % len(failedTestCases))
exit(1)
