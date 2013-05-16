#!/usr/bin/env python

"""Download the latest installer from the ci server

This script is used in the stage environment to retrieve the current installer.

"""

import subprocess
import json
import os.path
import glob

build_server = "ci"
dest_dir = os.path.expanduser("~")
server_url = 'http://localhost:8090'

def run_cmd(cmd): 
  print(" ".join(cmd))
  print("\n")
  return subprocess.check_output(cmd)


def get_artifacts_list(proj_name):
  build_info_cmd = ["ssh", "-T", build_server, "curl", "-s", "%s/job/%s/lastSuccessfulBuild/api/json" % (server_url, proj_name)]
  json_string = run_cmd(build_info_cmd)
  build_info = json.loads(json_string)
  artifacts = build_info["artifacts"]
  return artifacts
  
def get_artifact(proj_name, artifact):
  # Could use curl to retrieve the artifact, but this is complicated. Easier to just use scp...
  # curl_cmd = ["ssh", "-T", build_server, "curl", "-s" "%s/job/%s/lastSuccessfulBuild/artifact/%s" % (server_url, proj_name, artifact["relativePath"])]
  # print("Getting artifact: " + artifact["fileName"])
  
  artifacts_folder = "hudson/jobs"
  server_file = "%s:%s/%s/lastSuccessful/archive/%s" % (build_server, artifacts_folder, proj_name, artifact["relativePath"])
  local_file = dest_dir
  dl_cmd = ["scp", server_file, dest_dir]
  run_cmd(dl_cmd)
  
def get_files(proj_name, extension):
  artifacts = get_artifacts_list(proj_name)
  artifact_to_get = None
  for artifact in artifacts:
    if artifact["fileName"].endswith(extension):
      artifact_to_get = artifact
      break
  if artifact_to_get is None:
    return
  get_artifact(proj_name, artifact_to_get)


def clean_installer_dir():
  files_to_delete = glob.glob(os.path.join(dest_dir, "openBIS-installation-standard-technologies-*-*.tar.gz"))
  if len(files_to_delete) < 1:
    return
  print("Removing old installers : " + ",".join(files_to_delete))
  for file_to_delete in files_to_delete:
    os.remove(file_to_delete)

def get_files_from_server():
  get_files("installation", "tar.gz")


# # # # # # # # # # # # # # # # # # # # # # # # #
# The Script
clean_installer_dir()
get_files_from_server()
