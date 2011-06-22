#!/usr/bin/python
#
#
# @author:  Kaloyan Enimanev
#
from __future__ import with_statement
from datetime import date
import sys, string, xmlrpclib, re, os, getpass

DOWNLOAD_FOLDER="./tmp/"
confluenceToken = None
confluenceServer = xmlrpclib.ServerProxy('https://wiki-bsse.ethz.ch:8443/rpc/xmlrpc')
wikiText = ""


def printWiki(text=""):
  global wikiText
  wikiText += text
  wikiText += "\n"

def logIntoConfluence():
  global confluenceToken
  user = getpass.getuser()
  print "Please speficy Confluence password for user ", user
  password = getpass.getpass()
  confluenceToken = confluenceServer.confluence1.login(user, password)
  if confluenceToken is None:
      exit("Could not login page " + spacekey + ":" + pagetitle)


def uploadReleaseBinaryToConfluence(filename):
  # ugly, but I don't want to spend more time here
  filepath = DOWNLOAD_FOLDER + "/" + filename
  with open(filepath, 'rb') as f:
    data = f.read(); # slurp all the data
 
  spacekey="bis"
  pagetitle="Sprint Releases"
  
  if confluenceToken is None:
      logIntoConfluence()
      
  page = confluenceServer.confluence1.getPage(confluenceToken, spacekey, pagetitle)
  if page is None:
      exit("Could not find page " + spacekey + ":" + pagetitle)
 
  attachment = {}
  attachment['fileName'] = os.path.basename(filename)
  attachment['contentType'] = 'application/zip'
 
  print "Uploading {0} to confluence......".format(filename)
  confluenceServer.confluence1.addAttachment(confluenceToken, page['id'], attachment, xmlrpclib.Binary(data))

def fetchBinaries(version):
  print "Fetching {0} binaries from server ...".format(version)
  os.system("mkdir -p " + DOWNLOAD_FOLDER)
  os.system("rm {0}/*.zip".format(DOWNLOAD_FOLDER))
  os.system("scp sprint:~/fileserver/sprint_builds/openBIS/*-{0}*/*.* {1}".format(version, DOWNLOAD_FOLDER))

def printVersion(version):
  today = date.today().strftime("%d %B %Y")
  printWiki("h2. Version {0} ({1})".format(version, today))
  
def processFile(linkName, filePattern, version, listNestedLevels=1):
  fileName = findFile(filePattern + "-" + version)
  uploadReleaseBinaryToConfluence(fileName)
  nestedPrefix="*"*listNestedLevels
  printWiki("{0} [{1}|^{2}] ".format(nestedPrefix, linkName, fileName))
  
def uploadToConfluenceAndPrintPageText(version):
  printVersion(version)
  printWiki()
  printWiki("h5. openBIS Generic Framework")
  printWiki()
  processFile("Application Server (AS)", "openBIS-server", version)
  processFile("Data Store Server (DSS)", "datastore_server", version)
  processFile("DSS Client", "dss_client", version)
  printWiki("* [Documentation|^CISDDoc-{0}.html.zip]".format(version))
  printWiki()
  printWiki('h5. openBIS for High Content Screening')
  printWiki()
  printWiki("* Command Line Installation")
  processFile("Application Server (AS)", "openBIS-server-screening", version, 2)
  processFile("Data Store Server (DSS)", "datastore_server-screening", version, 2)

  processFile("Installation Wizard (AS+DSS)", "openBIS-installer-screening", version)

  processFile("API", "screening-api", version)

def findFile(filePattern):
  for file in os.listdir(DOWNLOAD_FOLDER):
      if file.startswith(filePattern):
          return file

if __name__ == '__main__':
    if len(sys.argv) < 2:
       exit("""
Usage: {0} <SPRINT-NUMBER>
Example command: {0} S104
         """.format(sys.argv[0]))
    version=sys.argv[1]
    fetchBinaries(version)
    uploadToConfluenceAndPrintPageText(version)
    print "===================================================================="
    print " Paste the following text on the Sprint Releases page in confluence "
    print "===================================================================="
    print wikiText