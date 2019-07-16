"""
Setup infrastructure common for all tests.
"""
import sys
import os.path
import time

# Base URL of the CI server which hosts the artifacts.
CI_BASE_URL = 'http://bs-ci03.ethz.ch:8090'

reuseRepository = False
devMode = False
cmd = sys.argv[0]
if len(sys.argv) > 1:
    firstArgument = sys.argv[1]
    if firstArgument == '-r':
        reuseRepository = True
    elif firstArgument == '-dr' or firstArgument == '-rd':
        reuseRepository = True
        devMode = True
    elif firstArgument == '-d':
        devMode = True
    elif firstArgument == '-h':
        print("Usage: %s [-h|-r|-d|-rd]\n-h: prints this help\n-r: reuses artifact repository\n"
            + "-d: developing mode\n-rd: both options" % os.path.basename(cmd))
        exit(1)
    else:
        print("Unknown option: %s. Use option '-h' to see usage." % firstArgument)
        exit(1)

dirname = os.path.dirname(os.path.abspath(__file__))
sys.path.append("%s/source" % dirname)
sys.path.append("%s/sourceTest" % dirname)

from systemtest.artifactrepository import JenkinsArtifactRepository 

REPOSITORY = JenkinsArtifactRepository(CI_BASE_URL, "%s/targets/artifact-repository" % dirname)
if not reuseRepository:
    REPOSITORY.clear()
