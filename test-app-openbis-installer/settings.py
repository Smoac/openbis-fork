"""
Setup infrastructure common for all tests.
"""
import sys
import os.path
import time

# Default base URL of the CI server which hosts the artifacts.
ci_base_url = 'http://bs-ci01.ethz.ch:8090'

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
    elif firstArgument == '-s':
        ci_base_url = sys.argv[2]
    elif firstArgument == '-h':
        print(("Usage: %s [-h|-r|-d|-rd|-s <ci server>]\n-h: prints this help\n-r: reuses artifact repository\n"
            + "-d: developing mode\n-rd: both options\n"
            + "-s <ci server>: option for CI server base URL") % os.path.basename(cmd))
        exit(1)
    else:
        print("Unknown option: %s. Use option '-h' to see usage." % firstArgument)
        exit(1)

dirname = os.path.dirname(os.path.abspath(__file__))
sys.path.append("%s/source" % dirname)
sys.path.append("%s/sourceTest" % dirname)

from systemtest.artifactrepository import JenkinsArtifactRepository 

REPOSITORY = JenkinsArtifactRepository(ci_base_url, "%s/targets/artifact-repository" % dirname)
if not reuseRepository:
    REPOSITORY.clear()
