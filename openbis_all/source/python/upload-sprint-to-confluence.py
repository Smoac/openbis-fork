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

  file_patterns = ['openBIS-server', 'openBIS-installation-standard-technologies', 'openBIS-clients-and-APIs', 'datastore_server', 'dss_client', 'datastore_server_plugin-yeastx']
  for file_pattern in file_patterns:
    os.system("scp sprint:/links/groups/cisd/sprint_builds/openBIS/*-{0}*/{1}-{0}*.* {2}".format(version, file_pattern, DOWNLOAD_FOLDER))

def printVersion(version, headerLevel):
  today = date.today().strftime("%d %B %Y")
  printWiki("h{2}. Version {0} ({1})".format(version, today, headerLevel))
  
def processFile(linkName, filePattern, version, listNestedLevels=1, pagetitle="Sprint Releases"):
  fileName = findFile(filePattern + "-" + version)
  uploadReleaseBinaryToConfluence(fileName, pagetitle)
  nestedPrefix="*"*listNestedLevels
  printWiki("{0} [{1}|^{2}] ".format(nestedPrefix, linkName, fileName))
  
def uploadToConfluenceAndPrintPageText(version):
  printVersion(version, 1)
  printWiki()
  printWiki("h5. openBIS for Standard Technologies")
  printWiki()
  processFile("Installation and Upgrade Wizard (AS+DSS)", "openBIS-installation-standard-technologies", version)
  processFile("Clients and APIs", "openBIS-clients-and-APIs", version)
  printWiki("* [Documentation|^CISDDoc-{0}.html.zip]".format(version))
  printWiki()

def uploadToConfluenceMetabolomicsAndPrintPageText(version):
  global wikiText
  wikiText = ""
  printVersion(version, 2)
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

  # cd to the tmp directory so the paths remain consistent -- do this after findFile, since that assumes a particular path
  current_dir = os.getcwd()
  os.chdir(DOWNLOAD_FOLDER)

  # unzip the yeastx plugin and set up the dir structure
  datastore_dir =  "datastore_server/"
  subprocess.call(["unzip", str(yeastx_plugin)])

  # update the datastore_server zip
  version_string = datastore_server[len("datastore_server"):len(datastore_server)]
  metabolomics_zip = "datastore_server_metabolomics" + version_string
  shutil.copy(str(datastore_server), metabolomics_zip)
  file_to_update = datastore_dir + "lib/datastore_server_plugin-yeastx.jar"
  subprocess.call(["zip", "-u", metabolomics_zip, file_to_update])

  # return to the original dir
  os.chdir(current_dir)


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
