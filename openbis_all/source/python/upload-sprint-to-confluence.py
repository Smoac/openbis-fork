#!/usr/bin/python
#
# Kaloyan: this script was created under time pressure, so its implementation is not optimal.
# Feel free to improve it.
#
# @author:  Kaloyan Enimanev
#
from __future__ import with_statement
from datetime import date
import sys, string, xmlrpclib, re, os, getpass, subprocess,  shutil

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
  print "Please specify Confluence password for user ", user
  password = getpass.getpass()
  confluenceToken = confluenceServer.confluence2.login(user, password)
  if confluenceToken is None:
      exit("Could not login page " + spacekey + ":" + pagetitle)


def uploadReleaseBinaryToConfluence(filename, pagetitle):
  # ugly, but I don't want to spend more time here
  filepath = DOWNLOAD_FOLDER + "/" + filename
  with open(filepath, 'rb') as f:
    data = f.read(); # slurp all the data
 
  spacekey="bis"
  
  if confluenceToken is None:
      logIntoConfluence()
      
  page = confluenceServer.confluence2.getPage(confluenceToken, spacekey, pagetitle)
  if page is None:
      exit("Could not find page " + spacekey + ":" + pagetitle)
 
  attachment = {}
  attachment['fileName'] = os.path.basename(filename)
  attachment['contentType'] = 'application/zip'
 
  print "Uploading {0} to confluence......".format(filename)
  confluenceServer.confluence2.addAttachment(confluenceToken, page['id'], attachment, xmlrpclib.Binary(data))

def fetchBinaries(version):
  print "Fetching {0} binaries from server ...".format(version)
  os.system("mkdir -p " + DOWNLOAD_FOLDER)
  os.system("rm {0}/*.zip".format(DOWNLOAD_FOLDER))
  os.system("scp sprint:~/fileserver/sprint_builds/openBIS/*-{0}*/*.* {1}".format(version, DOWNLOAD_FOLDER))

def printVersion(version):
  today = date.today().strftime("%d %B %Y")
  printWiki("h2. Version {0} ({1})".format(version, today))
  
def processFile(linkName, filePattern, version, listNestedLevels=1, pagetitle="Sprint Releases"):
  fileName = findFile(filePattern + "-" + version)
  uploadReleaseBinaryToConfluence(fileName, pagetitle)
  nestedPrefix="*"*listNestedLevels
  printWiki("{0} [{1}|^{2}] ".format(nestedPrefix, linkName, fileName))
  
def uploadToConfluenceAndPrintPageText(version):
  printVersion(version)
  printWiki()
  printWiki("h5. openBIS Generic Framework")
  printWiki()
  processFile("Installation and Upgrade Wizard (AS+DSS)", "openBIS-installation", version)
  printWiki("* Command Line Installation")
  processFile("Application Server (AS)", "openBIS-server", version, 2)
  processFile("Data Store Server (DSS)", "datastore_server", version, 2)
  processFile("DSS Client", "dss_client", version)
  processFile("KNIME Nodes", "ch.systemsx.cisd.openbis.knime", "")
  printWiki("* [Documentation|^CISDDoc-{0}.html.zip]".format(version))
  printWiki()
  printWiki('h5. openBIS for High Content Screening')
  printWiki()
  processFile("Installation and Upgrade Wizard (AS+DSS)", "openBIS-installation-screening", version)
  printWiki("* Command Line Installation")
  processFile("Application Server (AS)", "openBIS-server-screening", version, 2)
  processFile("Data Store Server (DSS)", "datastore_server-screening", version, 2)
  processFile("API", "screening-api", version)
  printWiki()
  printWiki('h5. openBIS for Proteomics')
  printWiki()
  processFile("Installation and Upgrade Wizard (AS+DSS)", "openBIS-installation-proteomics", version)
  printWiki("* Command Line Installation")
  processFile("Application Server (AS)", "openBIS-server-proteomics", version, 2)
  processFile("Data Store Server (DSS) plugin (generic DSS also needed)", "datastore_server_plugin-proteomics", version, 2)

def uploadToConfluenceMetabolomicsAndPrintPageText(version):
  global wikiText
  wikiText = ""
  printVersion(version)
  printWiki()
  printWiki("h5. openBIS for Metabolomics")
  printWiki()
  processFile("Application Server (AS)", "openBIS-server", version, 1, "openBIS Metabolomics")
  processFile("Data Store Server (DSS)", "datastore_server_metabolomics", version, 1, "openBIS Metabolomics")
  processFile("DSS Client", "dss_client", version, 1, "openBIS Metabolomics")
  printWiki()

def createMetabolomicsDssDist(version):
  # find the files we want to work with
  datastore_server = findFile("datastore_server" + "-" + version)
  yeastx_plugin = findFile("datastore_server_plugin-yeastx" + "-" + version)

  # unzip the yeastx plugin and set up the dir structure
  datastore_dir = DOWNLOAD_FOLDER + "datastore_server/"
  subprocess.call(["unzip", "-d" + datastore_dir, str(DOWNLOAD_FOLDER + yeastx_plugin)])
  
  # update the datastore_server zip
  version_string = datastore_server[len("datastore_server"):len(datastore_server)]
  metabolomics_zip = DOWNLOAD_FOLDER + "datastore_server_metabolomics" + version_string
  shutil.copy(str(DOWNLOAD_FOLDER + datastore_server), metabolomics_zip)
  file_to_update = datastore_dir + "lib/datastore_server_plugin-yeastx.jar"
  subprocess.call(["zip", "-u", metabolomics_zip, file_to_update])


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
    print " Link: https://wiki-bsse.ethz.ch/display/bis/Sprint+Releases        "
    print "===================================================================="
    print wikiText
    
    # Agios wants to access the yeastX version from this page
    createMetabolomicsDssDist(version)
    uploadToConfluenceMetabolomicsAndPrintPageText(version)
    print "========================================================================="
    print " Paste the following text on the openBIS Metabolomics page in confluence "
    print " Link: https://wiki-bsse.ethz.ch/display/bis/openBIS+Metabolomics        "
    print "========================================================================="
    print wikiText
