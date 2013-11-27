"""
Setup infrastructure common for all tests.
"""
import sys
import os.path
import time

# Base URL of the CI server which hosts the artifacts.
CI_BASE_URL = 'http://bs-ci01.ethz.ch:8090'

reuseRepository = False
cmd = sys.argv[0]
if len(sys.argv) > 1:
    firstArgument = sys.argv[1]
    if firstArgument == '-r':
        reuseRepository = True
    if firstArgument == '-h':
        print "Usage: %s [-h|-r]\n-h: prints this help\n-r: reuses artifact repository" % os.path.basename(cmd)
        exit(1)

dirname = os.path.dirname(cmd)
sys.path.append("%s/source" % dirname)
sys.path.append("%s/sourceTest" % dirname)

from systemtest.artifactrepository import JenkinsArtifactRepository 

REPOSITORY = JenkinsArtifactRepository(CI_BASE_URL, "%s/targets/artifact-repository" % dirname)
if not reuseRepository:
    REPOSITORY.clear()
